var clover = new Object();

// JSON: {classes : [{name, id, sl, el,  methods : [{sl, el}, ...]}, ...]}
clover.pageData = {"classes":[{"el":88,"id":519,"methods":[{"el":34,"sc":3,"sl":20},{"el":39,"sc":3,"sl":36},{"el":50,"sc":3,"sl":41},{"el":63,"sc":3,"sl":52},{"el":68,"sc":3,"sl":65},{"el":77,"sc":3,"sl":70},{"el":86,"sc":3,"sl":79}],"name":"RipServerTest","sl":19}]}

// JSON: {test_ID : {"methods": [ID1, ID2, ID3...], "name" : "testXXX() void"}, ...};
clover.testTargets = {"test_1":{"methods":[{"sl":79}],"name":"testPut","pass":true,"statements":[{"sl":81},{"sl":83},{"sl":85}]},"test_2":{"methods":[{"sl":70}],"name":"testPost","pass":true,"statements":[{"sl":72},{"sl":73},{"sl":75}]},"test_3":{"methods":[{"sl":41}],"name":"testDelete","pass":true,"statements":[{"sl":43},{"sl":45},{"sl":47},{"sl":49}]},"test_4":{"methods":[{"sl":65}],"name":"testGet","pass":true,"statements":[{"sl":67}]},"test_5":{"methods":[{"sl":52}],"name":"testDeterministic","pass":true,"statements":[{"sl":54},{"sl":55},{"sl":57},{"sl":59},{"sl":61}]}}

// JSON: { lines : [{tests : [testid1, testid2, testid3, ...]}, ...]};
clover.srcFileLines = [[], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [3], [], [3], [], [3], [], [3], [], [3], [], [], [5], [], [5], [5], [], [5], [], [5], [], [5], [], [], [], [4], [], [4], [], [], [2], [], [2], [2], [], [2], [], [], [], [1], [], [1], [], [1], [], [1], [], [], []]
