
javaDashboard.service('ModalUtils', function ($uibModal, $filter) {

    var translate = $filter('translate');

    this.html = function (content, style, size, callback) {
        $uibModal.open({
            templateUrl: 'admin/html/board/view/util/modal/html.html',
            windowTemplateUrl: 'admin/html/board/view/util/modal/window.html',
            backdrop: false,
            windowClass: style,
            size: size,
            controller: function ($scope, $uibModalInstance) {
                $scope.content = content;
                $scope.ok = function () {
                    $uibModalInstance.close();
                    if (callback) {
                        callback();
                    }
                };
            }
        });
    };

    this.alert = function (content, style, size, callback) {
        $uibModal.open({
            templateUrl: 'admin/html/board/view/util/modal/alert.html',
            windowTemplateUrl: 'admin/html/board/view/util/modal/window.html',
            backdrop: false,
            windowClass: style,
            size: size,
            controller: function ($scope, $uibModalInstance) {
                var emptyBody = translate('CONFIG.DASHBOARD.DASHBOARD_SOMETHING_WRONG');
                if (content instanceof Object) {
                    content.title ? $scope.title = content.title : $scope.title = translate('COMMON.TIP');
                    $scope.content = content.body ? content.body : emptyBody;
                } else {
                    $scope.title = translate('COMMON.TIP');
                    $scope.content = content ? content : emptyBody;
                }
                $scope.ok = function () {
                    $uibModalInstance.close();
                    if (callback) {
                        callback();
                    }
                };
            }
        });
    };

    this.confirm = function (content, style, size, ok, close) {
        $uibModal.open({
            templateUrl: 'admin/html/board/view/util/modal/confirm.html',
            windowTemplateUrl: 'admin/html/board/view/util/modal/window.html',
            backdrop: false,
            windowClass: style,
            size: size,
            controller: function ($scope, $uibModalInstance) {
                content ? $scope.content = content : $scope.content = translate('CONFIG.DASHBOARD.DASHBOARD_SOMETHING_WRONG');
                $scope.ok = function () {
                    $uibModalInstance.close();
                    if (ok) {
                        ok();
                    }
                };
                $scope.close = function () {
                    $uibModalInstance.close();
                    if (close) {
                        close();
                    }
                };
            }
        });
    };

    this.info = function (content, style, size, ok, close) {
        $uibModal.open({
            templateUrl: 'admin/html/board/view/util/modal/information.html',
            windowTemplateUrl: 'admin/html/board/view/util/modal/window.html',
            backdrop: false,
            windowClass: style,
            size: size,
            controller: function ($scope, $uibModalInstance) {
                content ? $scope.content = content : $scope.content = translate('CONFIG.DASHBOARD.DASHBOARD_SOMETHING_WRONG');
                $scope.ok = function () {
                    $uibModalInstance.close();
                    if (ok) {
                        ok();
                    }
                };
                $scope.close = function () {
                    $uibModalInstance.close();
                    if (close) {
                        close();
                    }
                };
            }
        });
    };
});