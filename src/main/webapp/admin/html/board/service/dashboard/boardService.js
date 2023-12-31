
javaDashboard.service('boardService', function ($http) {

    this.boardData;

    this.saveWidget = function (name, datasource, config) {
        var widget = {
            name: name,
            width: 12,
            datasource: datasource,
            config: angular.copy(config)//deep
        };
        if (!this.boardData) {
            var _this = this;
            this.get(function (bData) {
                bData.rows.push({widgets: [widget]});
                _this.saveThis();
            });
        } else {
            this.boardData.rows.push({widgets: [widget]});
            this.saveThis();
        }
    };

    this.saveThis = function () {
        $http.post("dash/board/saveData.do", {json: angular.toJson(this.boardData)}).success(function () {
        });
    };

    this.save = function (bData) {
        $http.post("dash/board/saveData.do", {json: angular.toJson(bData)}).success(function () {
        });
    };

    this.get = function (callback) {
        var _this = this;
        $http.get("dash/board/getData.do").success(function (response) {
            if (!response) {
                response = {rows: []};
            }
            _this.boardData = response;
            callback(_this.boardData);
        });
    };

});
