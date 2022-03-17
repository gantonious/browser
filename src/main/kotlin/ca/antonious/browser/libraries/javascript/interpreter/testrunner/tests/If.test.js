describe("If", () => {
  it("supports standalone if with block body", () => {
    let reachedFromTrue = false;
    let reachedFromFalse = false;

    if (true) {
      reachedFromTrue = true;
    }

    if (false) {
      reachedFromFalse = true;
    }

    expect(reachedFromTrue).toBeTrue();
    expect(reachedFromFalse).toBeFalse();
  });

  it("support standalone if with statement body", () => {
    let reachedFromTrue = false;
    let reachedFromFalse = false;

    if (true) reachedFromTrue = true;
    if (false) reachedFromFalse = true;

    expect(reachedFromTrue).toBeTrue();
    expect(reachedFromFalse).toBeFalse();
  });

  it("supports else clause with blocks", () => {
    for (let i = 0; i < 2; i++) {
      let reachedFromIfBlock = [false, false];

      if (i === 0) {
        reachedFromIfBlock[0] = true;
      } else {
        reachedFromIfBlock[1] = true;
      }

      for (let j = 0; j < 2; j++) {
        expect(reachedFromIfBlock[j]).toBe(i === j);
      }
    }
  });

  it("support else clause with statements", () => {
    for (let i = 0; i < 2; i++) {
      let reachedFromIfBlock = [false, false];

      if (i === 0) reachedFromIfBlock[0] = true;
      else reachedFromIfBlock[1] = true;

      for (let j = 0; j < 2; j++) {
        expect(reachedFromIfBlock[j]).toBe(i === j);
      }
    }
  });

  it("supports else if clauses with blocks", () => {
    for (let i = 0; i < 4; i++) {
      let reachedFromIfBlock = [false, false, false, false];

      if (i === 0) {
        reachedFromIfBlock[0] = true;
      } else if (i === 1) {
        reachedFromIfBlock[1] = true;
      } else if (i === 2) {
        reachedFromIfBlock[2] = true;
      } else {
        reachedFromIfBlock[3] = true;
      }

      for (let j = 0; j < 4; j++) {
        expect(reachedFromIfBlock[j]).toBe(i === j);
      }
    }
  });

  it("support else if clauses with statements", () => {
    for (let i = 0; i < 4; i++) {
      let reachedFromIfBlock = [false, false, false, false];

      if (i === 0) reachedFromIfBlock[0] = true;
      else if (i === 1) reachedFromIfBlock[1] = true;
      else if (i === 2) reachedFromIfBlock[2] = true;
      else reachedFromIfBlock[3] = true;

      for (let j = 0; j < 4; j++) {
        expect(reachedFromIfBlock[j]).toBe(i === j);
      }
    }
  });
});
