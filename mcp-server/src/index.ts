#!/usr/bin/env node

import { Server } from "@modelcontextprotocol/sdk/server/index.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import {
  CallToolRequestSchema,
  ListToolsRequestSchema,
} from "@modelcontextprotocol/sdk/types.js";
import axios from "axios";

const OVERLOAD_API_URL = process.env.OVERLOAD_API_URL || "http://localhost:8080";
const OVERLOAD_MASTER_SECRET = process.env.OVERLOAD_API_TOKEN || process.env.OVERLOAD_MASTER_SECRET || "overload_mcp_secret_8080";

let activeSessionToken: string | null = null;

async function getValidToken(): Promise<string> {
  if (activeSessionToken) return activeSessionToken;
  try {
    const authRes = await axios.post(
      `${OVERLOAD_API_URL}/api/v1/auth`,
      { masterSecret: OVERLOAD_MASTER_SECRET },
      { headers: { "X-Master-Secret": OVERLOAD_MASTER_SECRET }, timeout: 5000 }
    );
    if (authRes.data?.sessionToken) {
      activeSessionToken = authRes.data.sessionToken;
      return activeSessionToken!;
    }
  } catch (_err) {
    // Fallback to direct token
  }
  return OVERLOAD_MASTER_SECRET;
}

const apiClient = axios.create({
  baseURL: OVERLOAD_API_URL,
  headers: {
    "Content-Type": "application/json",
  },
  timeout: 10000,
});

apiClient.interceptors.request.use(async (config) => {
  const token = await getValidToken();
  config.headers.Authorization = `Bearer ${token}`;
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401 && activeSessionToken) {
      // Invalidate expired session token so next request re-performs handshake
      activeSessionToken = null;
    }
    return Promise.reject(error);
  }
);

const server = new Server(
  {
    name: "overload-mcp-server",
    version: "1.0.0",
  },
  {
    capabilities: {
      tools: {},
    },
  }
);

server.setRequestHandler(ListToolsRequestSchema, async () => {
  return {
    tools: [
      {
        name: "fetch_workout_history",
        description:
          "Fetches completed workout sessions, total volume, set metrics, and muscle group breakdown from Overload Android app.",
        inputSchema: {
          type: "object",
          properties: {
            timeRange: {
              type: "string",
              enum: ["LAST_7_DAYS", "LAST_30_DAYS", "ALL_TIME"],
              description: "Time range filter for past workout history",
              default: "ALL_TIME",
            },
          },
        },
      },
      {
        name: "fetch_exercise_library",
        description:
          "Fetches exercise library from Overload app (contains valid exercise IDs, names, muscle categories, and equipment) needed for formulating new splits.",
        inputSchema: {
          type: "object",
          properties: {
            category: {
              type: "string",
              description: "Optional muscle category filter (e.g. 'Chest', 'Back', 'Legs')",
            },
          },
        },
      },
      {
        name: "fetch_current_splits",
        description:
          "Fetches active workout split templates (workout groups) and their assigned exercise sequences.",
        inputSchema: {
          type: "object",
          properties: {},
        },
      },
      {
        name: "replace_workout_splits",
        description:
          "Atomically wipes all existing workout splits in Overload and replaces them with new splits formulated by the agent.",
        inputSchema: {
          type: "object",
          properties: {
            confirmReplace: {
              type: "boolean",
              description: "Safety flag to confirm replacing all existing splits",
              default: true,
            },
            splits: {
              type: "array",
              description: "List of new splits to insert",
              items: {
                type: "object",
                properties: {
                  name: {
                    type: "string",
                    description: "Name of the new split routine (e.g. 'Push Day A')",
                  },
                  notes: {
                    type: "string",
                    description: "Target focus or notes for the workout",
                  },
                  exerciseIds: {
                    type: "array",
                    items: { type: "string" },
                    description: "Ordered array of valid exercise IDs from exercise library",
                  },
                },
                required: ["name", "exerciseIds"],
              },
            },
          },
          required: ["confirmReplace", "splits"],
        },
      },
    ],
  };
});

server.setRequestHandler(CallToolRequestSchema, async (request) => {
  const { name, arguments: args } = request.params;

  try {
    if (name === "fetch_workout_history") {
      const timeRange = (args?.timeRange as string) || "ALL_TIME";
      const response = await apiClient.get("/api/v1/workout-history", {
        params: { timeRange },
      });
      return {
        content: [
          {
            type: "text",
            text: JSON.stringify(response.data, null, 2),
          },
        ],
      };
    }

    if (name === "fetch_exercise_library") {
      const category = args?.category as string | undefined;
      const response = await apiClient.get("/api/v1/exercise-library", {
        params: category ? { category } : {},
      });
      return {
        content: [
          {
            type: "text",
            text: JSON.stringify(response.data, null, 2),
          },
        ],
      };
    }

    if (name === "fetch_current_splits") {
      const response = await apiClient.get("/api/v1/splits");
      return {
        content: [
          {
            type: "text",
            text: JSON.stringify(response.data, null, 2),
          },
        ],
      };
    }

    if (name === "replace_workout_splits") {
      const confirmReplace = args?.confirmReplace !== false;
      const splits = args?.splits;

      if (!splits || !Array.isArray(splits)) {
        throw new Error("Invalid parameters: 'splits' array is required.");
      }

      const response = await apiClient.post("/api/v1/splits/replace", {
        confirmReplace,
        splits,
      });

      return {
        content: [
          {
            type: "text",
            text: JSON.stringify(response.data, null, 2),
          },
        ],
      };
    }

    throw new Error(`Unknown tool name: ${name}`);
  } catch (error: any) {
    const errorMsg =
      error?.response?.data?.error || error?.message || "Unknown error occurred";
    return {
      isError: true,
      content: [
        {
          type: "text",
          text: `Overload MCP Server Error: ${errorMsg}. Make sure the Overload app is open and MCP server is turned on in app Settings.`,
        },
      ],
    };
  }
});

async function main() {
  const transport = new StdioServerTransport();
  await server.connect(transport);
  console.error("Overload MCP Server running on stdio transport.");
}

main().catch((err) => {
  console.error("Fatal error starting Overload MCP server:", err);
  process.exit(1);
});
