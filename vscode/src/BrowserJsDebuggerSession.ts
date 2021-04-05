import {
  InitializedEvent,
  LoggingDebugSession,
  OutputEvent,
  Scope,
  Source,
  StoppedEvent,
  TerminatedEvent,
  Thread,
} from "vscode-debugadapter";

import { DebugProtocol } from "vscode-debugprotocol";
import WebSocket = require("ws");

type DebuggerResponse =
  | { type: "breakpoint_hit"; line: number }
  | { type: "eval_finished"; result: string }
  | {
      type: "get_stack_response";
      frames: { name: string; line: number; column: number }[];
    }
  | {
      type: "get_variables_response";
      variables: { name: string; type: string; value: string }[];
    };

type DebuggerRequest =
  | { type: "set_breakpoint"; line: number }
  | {
      type: "set_breakpoints";
      breakpoints: { line: number; filename: string }[];
    }
  | { type: "execute"; command: string }
  | { type: "continue" }
  | { type: "get_stack" }
  | { type: "get_variables"; scopeName: string };

export class BrowserJsDebuggerSession extends LoggingDebugSession {
  private debuggerSocket = new WebSocket("ws://localhost:31256");

  private breakpoints: Record<number, DebugProtocol.Breakpoint> = {};
  private pendingVariablesResponse: DebugProtocol.VariablesResponse | null = null;
  private pendingEvaluationResponse: DebugProtocol.EvaluateResponse | null = null;
  private pendingStackTraceResponse: DebugProtocol.StackTraceResponse | null = null;

  constructor() {
    super();
    this.setDebuggerLinesStartAt1(false);
    this.setDebuggerColumnsStartAt1(false);

    this.debuggerSocket.onclose = () => {
      this.sendEvent(new OutputEvent("Debugger server disconnected"));
      this.sendEvent(new TerminatedEvent());
    };

    this.debuggerSocket.onmessage = (message) => {
      const debuggerResponse: DebuggerResponse = JSON.parse(
        message.data.toString()
      );

      switch (debuggerResponse.type) {
        case "breakpoint_hit":
          this.sendEvent(new StoppedEvent("breakpoint", 0));
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
                  name: frame.name,
                  line: this.convertDebuggerLineToClient(frame.line),
                  column: this.convertDebuggerColumnToClient(frame.column),
                  source: new Source(
                    "jquery.js",
                    "/Users/george/dev/browser/jquery.js"
                  ),
                };
              }
            );

            this.sendResponse(this.pendingStackTraceResponse);
            this.pendingStackTraceResponse = null;
          }
          break;
        case "get_variables_response":
          if (this.pendingVariablesResponse) {
            this.pendingVariablesResponse.body =
              this.pendingVariablesResponse.body ?? {};

            this.pendingVariablesResponse.body.variables = debuggerResponse.variables.map(
              (variable) => {
                return {
                  name: variable.name,
                  type: variable.type,
                  value: variable.value,
                  variablesReference: 0,
                };
              }
            );

            this.sendResponse(this.pendingVariablesResponse);
            this.pendingEvaluationResponse = null;
          }
          break;
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
    (args.breakpoints ?? []).forEach((breakpoint) => {
      this.breakpoints[breakpoint.line] = { ...breakpoint, verified: true };
    });

    this.sendDebuggerRequest({
      type: "set_breakpoints",
      breakpoints: (args.breakpoints ?? []).map((breakpoint) => {
        return {
          filename: args.source.name ?? "unknown",
          line: this.convertClientColumnToDebugger(breakpoint.line),
        };
      }),
    });

    response.body = {
      breakpoints: [{ verified: true }],
    };
  }

  protected threadsRequest(response: DebugProtocol.ThreadsResponse) {
    response.body = {
      threads: [new Thread(0, "js")],
    };
    this.sendResponse(response);
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

  protected scopesRequest(
    response: DebugProtocol.ScopesResponse,
    args: DebugProtocol.ScopesArguments
  ): void {
    response.body = {
      scopes: [new Scope("Local", 1, false)],
    };
    this.sendResponse(response);
  }

  protected variablesRequest(
    response: DebugProtocol.VariablesResponse,
    args: DebugProtocol.VariablesArguments
  ) {
    this.pendingVariablesResponse = response;
    this.sendDebuggerRequest({
      type: "get_variables",
      scopeName: "local",
    });
  }

  private sendDebuggerRequest(request: DebuggerRequest) {
    this.debuggerSocket.send(JSON.stringify(request));
  }
}
