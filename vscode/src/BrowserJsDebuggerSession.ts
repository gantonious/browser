import {
  Breakpoint,
  BreakpointEvent,
  InitializedEvent,
  LoggingDebugSession,
} from "vscode-debugadapter";

import { DebugProtocol } from "vscode-debugprotocol";
import WebSocket = require("ws");

type DebuggerResponse =
  | { type: "breakpoint_hit"; line: number }
  | { type: "eval_finished"; result: string }
  | {
      type: "get_stack_response";
      frames: { name: string; line: number; column: number }[];
    };

type DebuggerRequest =
  | { type: "set_breakpoint"; line: number }
  | { type: "execute"; command: string }
  | { type: "continue" }
  | { type: "get_stack" };

export class BrowserJsDebuggerSession extends LoggingDebugSession {
  private debuggerSocket = new WebSocket("ws://localhost:31256");

  private pendingEvaluationResponse: DebugProtocol.EvaluateResponse | null = null;
  private pendingStackTraceResponse: DebugProtocol.StackTraceResponse | null = null;

  constructor() {
    super();
    this.setDebuggerLinesStartAt1(false);
    this.setDebuggerColumnsStartAt1(false);

    this.debuggerSocket.onmessage = (message) => {
      const debuggerResponse: DebuggerResponse = JSON.parse(
        message.data.toString()
      );

      switch (debuggerResponse.type) {
        case "breakpoint_hit":
          this.sendEvent(
            new BreakpointEvent(
              "breakpoint",
              new Breakpoint(true, debuggerResponse.line)
            )
          );
          break;
        case "eval_finished":
          if (this.pendingEvaluationResponse) {
            try {
              this.pendingEvaluationResponse.body =
                this.pendingEvaluationResponse.body ?? {};
              this.pendingEvaluationResponse.body.result =
                debuggerResponse.result;
              this.sendResponse(this.pendingEvaluationResponse);
            } catch (error) {}
            this.pendingEvaluationResponse = null;
          }
          break;
        case "get_stack_response":
          if (this.pendingStackTraceResponse) {
            this.pendingStackTraceResponse.body =
              this.pendingStackTraceResponse.body ?? {};

            this.pendingStackTraceResponse.body.totalFrames =
              debuggerResponse.frames.length;

            this.pendingStackTraceResponse.body.stackFrames = debuggerResponse.frames.map(
              (frame, index) => {
                return {
                  id: index,
                  ...frame,
                };
              }
            );

            this.sendResponse(this.pendingStackTraceResponse);
            this.pendingStackTraceResponse = null;
          }
        default:
          break;
      }
    };
  }

  protected async initializeRequest(
    response: DebugProtocol.InitializeResponse,
    args: DebugProtocol.InitializeRequestArguments
  ) {
    response.body = response.body ?? {};
    this.sendResponse(response);
    this.sendEvent(new InitializedEvent());
  }

  protected async evaluateRequest(
    response: DebugProtocol.EvaluateResponse,
    args: DebugProtocol.EvaluateArguments
  ) {
    this.pendingEvaluationResponse = response;

    this.sendDebuggerRequest({
      type: "execute",
      command: args.expression,
    });
  }

  protected async setBreakPointsRequest(
    response: DebugProtocol.SetBreakpointsResponse,
    args: DebugProtocol.SetBreakpointsArguments
  ): Promise<void> {
    this.sendDebuggerRequest({
      type: "set_breakpoint",
      line: args.breakpoints![0]?.line ?? 0,
    });

    response.body = {
      breakpoints: [{ verified: true }],
    };
  }

  protected continueRequest(
    response: DebugProtocol.ContinueResponse,
    args: DebugProtocol.ContinueArguments
  ): void {
    this.sendDebuggerRequest({ type: "continue" });
    this.sendResponse(response);
  }

  protected stackTraceRequest(
    response: DebugProtocol.StackTraceResponse,
    args: DebugProtocol.StackTraceArguments
  ) {
    this.pendingStackTraceResponse = response;
    this.sendDebuggerRequest({ type: "get_stack" });
  }

  private sendDebuggerRequest(request: DebuggerRequest) {
    this.debuggerSocket.send(JSON.stringify(request));
  }
}
