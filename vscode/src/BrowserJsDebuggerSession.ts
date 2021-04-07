import {
  Handles,
  InitializedEvent,
  LoggingDebugSession,
  OutputEvent,
  Scope,
  StoppedEvent,
  TerminatedEvent,
  Thread,
} from "vscode-debugadapter";

import { DebugProtocol } from "vscode-debugprotocol";
import axios from "axios";

import WebSocket = require("ws");

type DebuggerResponse = { type: "breakpoint_hit"; line: number };

export class BrowserJsDebuggerSession extends LoggingDebugSession {
  private variableHandles = new Handles<string>();

  private httpClient = axios.create({
    responseType: "json",
    baseURL: "http://localhost:31256/",
  });

  constructor() {
    super();
    this.setDebuggerLinesStartAt1(false);
    this.setDebuggerColumnsStartAt1(false);

    let debuggerSocket: WebSocket;

    try {
      debuggerSocket = new WebSocket("ws://localhost:31256/events");
      this.sendEvent(new OutputEvent(`Connected to debugger on port 31256`));
    } catch (error) {
      this.sendEvent(
        new OutputEvent(`Failed to connect to debugger: ${error}`, "error")
      );
      this.sendEvent(new TerminatedEvent());
      return;
    }

    debuggerSocket.onclose = () => {
      this.sendEvent(new OutputEvent("Remote debugger ended session"));
      this.sendEvent(new TerminatedEvent());
    };

    debuggerSocket.onmessage = (message) => {
      const debuggerResponse: DebuggerResponse = JSON.parse(
        message.data.toString()
      );

      switch (debuggerResponse.type) {
        case "breakpoint_hit":
          this.sendEvent(new StoppedEvent("breakpoint", 0));
          break;
      }
    };
  }

  protected async initializeRequest(
    response: DebugProtocol.InitializeResponse,
    args: DebugProtocol.InitializeRequestArguments
  ) {
    response.body = response.body ?? {};
    const debuggerResponse = await this.httpClient.get("status");
    const statusResponse = debuggerResponse.data as {
      status: "Running" | "Paused";
    };

    this.sendResponse(response);
    this.sendEvent(new InitializedEvent());

    if (statusResponse.status === "Paused") {
      this.sendEvent(new StoppedEvent("breakpoint", 0));
    }
  }

  protected async evaluateRequest(
    response: DebugProtocol.EvaluateResponse,
    args: DebugProtocol.EvaluateArguments
  ) {
    const body = {
      javascript: args.expression,
    };

    const debuggerResponse = await this.httpClient.post("evaluate", body);
    const evaluationResponse = debuggerResponse.data as { result: string };

    response.body = response.body ?? {};
    response.body.result = evaluationResponse.result;
    this.sendResponse(response);
  }

  protected async setBreakPointsRequest(
    response: DebugProtocol.SetBreakpointsResponse,
    args: DebugProtocol.SetBreakpointsArguments
  ) {
    const body = {
      breakpoints: (args.breakpoints ?? []).map((breakpoint) => {
        return {
          filename: args.source.name ?? "unknown",
          line: this.convertClientColumnToDebugger(breakpoint.line),
        };
      }),
    };

    await this.httpClient.post("breakpoints", body);

    response.body = {
      breakpoints: [{ verified: true }],
    };
    this.sendResponse(response);
  }

  protected threadsRequest(response: DebugProtocol.ThreadsResponse) {
    response.body = {
      threads: [new Thread(0, "js")],
    };
    this.sendResponse(response);
  }

  protected async continueRequest(
    response: DebugProtocol.ContinueResponse,
    args: DebugProtocol.ContinueArguments
  ) {
    await this.httpClient.post("continue");
    this.variableHandles.reset();
    this.sendResponse(response);
  }

  protected async nextRequest(
    response: DebugProtocol.NextResponse,
    args: DebugProtocol.NextArguments
  ) {
    await this.httpClient.post("stepOver");
    this.sendResponse(response);
  }

  protected async stackTraceRequest(
    response: DebugProtocol.StackTraceResponse,
    args: DebugProtocol.StackTraceArguments
  ) {
    const debuggerResponse = await this.httpClient.get("stack");

    const stackInfo = debuggerResponse.data as {
      frames: {
        name: string;
        line: number;
        column: number;
        filename: string;
      }[];
    };

    response.body = response.body ?? {};

    response.body.totalFrames = stackInfo.frames.length;
    response.body.stackFrames = stackInfo.frames.map((frame, index) => {
      return {
        id: index,
        name: frame.name,
        line: this.convertDebuggerLineToClient(frame.line),
        column: this.convertDebuggerColumnToClient(frame.column),
        source: { name: frame.filename },
      };
    });

    this.sendResponse(response);
  }

  protected async sourceRequest(
    response: DebugProtocol.SourceResponse,
    args: DebugProtocol.SourceArguments
  ) {
    const debuggerResponse = await this.httpClient.get(
      `source/${args.source?.name}`
    );

    const sourceResponse = debuggerResponse.data as { source: string };

    response.body = response.body ?? {};
    response.body.content = sourceResponse.source;

    this.sendResponse(response);
  }

  protected scopesRequest(
    response: DebugProtocol.ScopesResponse,
    args: DebugProtocol.ScopesArguments
  ): void {
    response.body = {
      scopes: [
        new Scope(
          "local",
          this.variableHandles.create(args.frameId.toString()),
          false
        ),
        new Scope(
          "this",
          this.variableHandles.create(`${args.frameId.toString()}/this`),
          false
        ),
        new Scope("global", this.variableHandles.create("global"), false),
      ],
    };
    this.sendResponse(response);
  }

  protected async variablesRequest(
    response: DebugProtocol.VariablesResponse,
    args: DebugProtocol.VariablesArguments
  ) {
    const debuggerResponse = await this.httpClient.get(
      `variables/${this.variableHandles.get(args.variablesReference)}`
    );

    const variablesResponse = debuggerResponse.data as {
      variables: {
        name: string;
        type: string;
        value: string;
        expandPath?: string;
      }[];
    };

    response.body = response.body ?? {};
    response.body.variables = variablesResponse.variables.map((variable) => {
      return {
        name: variable.name,
        type: variable.type,
        value: variable.value,
        variablesReference: variable.expandPath
          ? this.variableHandles.create(variable.expandPath)
          : 0,
      };
    });

    this.sendResponse(response);
  }
}
