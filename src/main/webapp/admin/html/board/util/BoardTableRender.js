var BoardTableRender = function (jqContainer, options, drill) {
    this.container = jqContainer; // jquery object
    this.options = options;
    this.tall;
    this.drill = drill;
    var _this = this;
    $(this.container).resize(function (e) {
        _this.resize(e.target);
    });
};

BoardTableRender.prototype.resize = function (container) {
    var wrapper = $(container).find('.table_wrapper');
    wrapper.css('width', 'auto');
    if (wrapper.width() < $(container).width()) {
        wrapper.css('width', '100%');
    }
};

BoardTableRender.prototype.do = function (tall, persist) {
    this.tall = tall;
    tall = _.isUndefined(tall) ? 500 : tall;
    var divHeight = tall - 90;
    var _this = this;
    var render = function (o, drillConfig) {
        _this.options = o;
        if (_this.drill) {
            _this.drill.config = drillConfig;
        }
        _this.do(_this.tall);
    };
    var args = {
        tall: divHeight,
        chartConfig: this.options.chartConfig,
        data: this.options.data,
        container: this.container,
        drill: this.drill,
        render: render
    };
    new CBCrossTable(args).table();
    $(this.container).css({
        height: tall + "px"
    });
    this.resize(this.container);
    if (persist) {
        persist.data = this.options.data;
        persist.type = "table"
    }
    return render;
};

