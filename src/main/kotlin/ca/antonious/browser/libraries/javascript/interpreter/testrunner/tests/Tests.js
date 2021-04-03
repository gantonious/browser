test("Test addition", () => {
    function CustomFunction() { }
    function CustomValueOf() { }
    CustomValueOf.prototype.valueOf = function() { return 2 };

    expect(2 + 2).toBe(4);
    expect(2 + '2').toBe('22');
    expect(2 + new CustomValueOf()).toBe(4);
});

test("Test accessing variable from nested scope", () => {
    let a = 2;

    if (true) {
        expect(a).toBe(2);
    }
});

test("Test updating variable from nested scope", () => {
    let a = 2;

    if (true) {
        a = 4;
    }

    expect(a).toBe(4);
});
