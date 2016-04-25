(function() {
    'use strict';
    angular
        .module('reporttestApp')
        .factory('Student', Student);

    Student.$inject = ['$resource'];

    function Student ($resource) {
        var resourceUrl =  'api/students/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'exportPDF': {method: 'GET',isArray: true, params: {id:'@id'}, url: 'api/exportPDF'},
            'exportWord': {method: 'GET',isArray: true, params: {id:'@id'}, url: 'api/exportWord'},
            'exportHTML': {method: 'GET',isArray: true, params: {id:'@id'}, url: 'api/exportHTML'},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    data = angular.fromJson(data);
                    return data;
                }
            },
            'update': { method:'PUT' }
        });
    }
})();
