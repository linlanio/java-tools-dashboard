
'use strict';
javaDashboard.service('chartTableService', function () {

    this.render = function (containerDom, option, scope, persist, drill) {
        if (option == null) {
            containerDom.html("<div class=\"alert alert-danger\" role=\"alert\">No Data!</div>");
            return;
        }
        var height;
        scope ? height = scope.myheight : null;
        return new BoardTableRender(containerDom, option, drill).do(height, persist);
    };

    this.parseOption = function (data) {
        var tableOption = chartDataProcess(data.chartConfig, data.keys, data.series, data.data, data.seriesConfig);
        return tableOption;
    };
});