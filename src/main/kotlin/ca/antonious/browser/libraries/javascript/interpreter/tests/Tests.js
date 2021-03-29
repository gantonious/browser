test("Test addition", () => {
    function CustomFunction() { }
    function CustomValueOf() { }
    CustomValueOf.prototype.valueOf = function() { return 2 };

    expect(2 + 2).toBe(4);
    expect(2 + '2').toBe('22');
    expect(2 + new CustomValueOf()).toBe(4);
});
