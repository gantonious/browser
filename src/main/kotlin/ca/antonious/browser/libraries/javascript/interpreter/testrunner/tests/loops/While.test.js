describe("While", () => {
  it("never invokes block if conditions is not truthy", () => {
    let loopInvocations = 0;

    while (false) {
      loopInvocations++;
    }

    expect(loopInvocations).toBe(0);
  });

  it("invokes block until condition is false", () => {
    let loopInvocations = 0;

    while (loopInvocations < 5) {
      loopInvocations++;
    }

    expect(loopInvocations).toBe(5);
  });

  it("invokes statement until condition is false", () => {
    let loopInvocations = 0;

    while (loopInvocations < 5) loopInvocations++;

    expect(loopInvocations).toBe(5);
  });

  it("exits early if break is called", () => {
    let loopInvocations = 0;

    while (loopInvocations < 5) {
      if (loopInvocations == 2) {
        break;
      }
      loopInvocations++;
    }

    expect(loopInvocations).toBe(2);
  });

  it("exits block early if continue is called", () => {
    let loopInvocations = 0;
    let postContinueInvocations = 0;

    while (loopInvocations < 5) {
      loopInvocations++;
      if (loopInvocations == 2) {
        continue;
      }
      postContinueInvocations++;
    }

    expect(postContinueInvocations).toBe(4);
  });
});
