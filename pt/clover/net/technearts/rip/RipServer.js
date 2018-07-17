var clover = new Object();

// JSON: {classes : [{name, id, sl, el,  methods : [{sl, el}, ...]}, ...]}
clover.pageData = {"classes":[{"el":139,"id":398,"methods":[{"el":36,"sc":3,"sl":34},{"el":46,"sc":3,"sl":44},{"el":61,"sc":3,"sl":55},{"el":72,"sc":3,"sl":68},{"el":86,"sc":3,"sl":80},{"el":98,"sc":3,"sl":92},{"el":106,"sc":3,"sl":100},{"el":111,"sc":3,"sl":108},{"el":120,"sc":3,"sl":113},{"el":125,"sc":3,"sl":122},{"el":133,"sc":3,"sl":127},{"el":138,"sc":3,"sl":135}],"name":"RipServer","sl":25}]}

// JSON: {test_ID : {"methods": [ID1, ID2, ID3...], "name" : "testXXX() void"}, ...};
clover.testTargets = {"test_1":{"methods":[{"sl":122}],"name":"testGet","pass":true,"statements":[{"sl":124}]},"test_2":{"methods":[{"sl":122}],"name":"testDeterministic","pass":true,"statements":[{"sl":124}]},"test_3":{"methods":[{"sl":122}],"name":"testPut","pass":true,"statements":[{"sl":124}]},"test_4":{"methods":[{"sl":122}],"name":"testPost","pass":true,"statements":[{"sl":124}]},"test_5":{"methods":[{"sl":122}],"name":"testDelete","pass":true,"statements":[{"sl":124}]},"test_6":{"methods":[{"sl":122}],"name":"testBasicTemplate","pass":true,"statements":[{"sl":124}]}}

// JSON: { lines : [{tests : [testid1, testid2, testid3, ...]}, ...]};
clover.srcFileLines = [[], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [], [2, 3, 4, 1, 6, 5], [], [2, 3, 4, 1, 6, 5], [], [], [], [], [], [], [], [], [], [], [], [], [], [], []]
