describe("String.prototype.replace", () => {
  test("returns copy of string if no arguments are passed", () => {
    expect("Hello, world!".replace()).toBe("Hello, world!");
  });

  test("returns copy of string if nothing matches pattern", () => {
    expect("Hello, world!".replace("word", "new word")).toBe("Hello, world!");
  });

  test("replaces first match with undefined if no replacement is provided", () => {
    expect("Hello, world! Goodbye, world!".replace("world")).toBe(
      "Hello, undefined! Goodbye, world!"
    );
  });

  test("replaces first occurrence of string pattern", () => {
    expect("Hello, world! Goodbye, world!".replace("world", "tester")).toBe(
      "Hello, tester! Goodbye, world!"
    );
  });

  test("replaces first occurrence of regex pattern", () => {
    expect("Hello, world! Goodbye, world!".replace(/w\w+/, "tester")).toBe(
      "Hello, tester! Goodbye, world!"
    );
  });

  test("replaces first occurrence of pattern with output of replacer function", () => {
    function replacer(match) {
      return match.toUpperCase();
    }

    expect("Hello, world! Goodbye, world!".replace("world", replacer)).toBe(
      "Hello, WORLD! Goodbye, world!"
    );
  });

  test("invokes replacer function correctly for string pattern", () => {
    let replacerInvocation;

    function replacer() {
      replacerInvocation = arguments;
    }

    "Test: Hello, world! Hello, world!".replace("Hello, world!", replacer);

    expect(replacerInvocation).toBe([
      "Hello, world!",
      6,
      "Test: Hello, world! Hello, world!",
    ]);
  });

  test("invokes replacer function correctly for regex pattern with groups", () => {
    let replacerInvocation;

    function replacer() {
      replacerInvocation = arguments;
    }

    "Test: Hello, world! Hello, world!".replace(/(Hello), (world)!/, replacer);

    expect(replacerInvocation).toBe([
      "Hello, world!",
      "Hello",
      "world",
      6,
      "Test: Hello, world! Hello, world!",
    ]);
  });
});
