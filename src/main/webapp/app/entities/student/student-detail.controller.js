(function() {
    'use strict';

    angular
        .module('reporttestApp')
        .controller('StudentDetailController', StudentDetailController);

    StudentDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'entity', 'Student'];

    function StudentDetailController($scope, $rootScope, $stateParams, entity, Student) {
        var vm = this;
        vm.student = entity;
        
        var unsubscribe = $rootScope.$on('reporttestApp:studentUpdate', function(event, result) {
            vm.student = result;
        });
        $scope.$on('$destroy', unsubscribe);

    }
})();
