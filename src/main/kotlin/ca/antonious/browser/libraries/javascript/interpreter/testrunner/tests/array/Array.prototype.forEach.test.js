test("Array.prototype.forEach: calls passed function for each value in array in order", () => {
  let forEachValues = [];
  let arrayToIterate = [1, "2", true];

  arrayToIterate.forEach((value) => forEachValues.push(value));
  expect(forEachValues).toBe(arrayToIterate);
});
