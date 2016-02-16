/*
 * Copyright (C) 2016 Robert Thornton. All rights reserved.
 * This notice may not be removed.
 */
(function () {
    "use strict";

    hljs.initHighlightingOnLoad();

    document.onreadystatechange = function (e) {
        if ("complete" !== document.readyState) {
            return;
        }
        initialize();
    };

    function initialize() {
        $(".edit-page").click(editPage);
        $(".add-page").click(addPage);
        $(".save-page").click(savePage);
        $(".cancel-page").click(cancelPage);
    }

    function editPage() {
        var format = $('#source-format').val();
        var path = $('#source-path').val().replace(/\.html/, format);
        var uri = "/api" + path;
        console.info("GET " + uri);
        sendRequest("GET", uri, null, function() {
            if (this.status == 200) {
                console.info(this.status + " " + this.statusText);
                $('#editor-content').val(this.responseText);

                $('.edit-page').addClass('hidden');
                $('.add-page').addClass('hidden');
                $('.save-page').removeClass('hidden');
                $('.cancel-page').removeClass('hidden');
                $('.form-group').removeClass('hidden');

                $('.page-title').addClass('hidden');
                $('.form-group').removeClass('hidden');

                $('#editor-title').val($('#source-title').val());
                $('#editor-path').val($('#source-path').val());
                $('#editor-format').val($('#source-format').val());

                $('#rendered-content').addClass('hidden');
                $('#page-editor').removeClass('hidden');
                $('#editor-content').each(function () {
                    var el = document.getElementById('editor-content');
                    $(el).focus();
                    el.selectionStart = 0;
                    el.selectionEnd = 0;
                    el.scrollTop = 0;
                });
            } else {
                console.error(this.status + " " + this.statusText);
            }
        });
    }

    function addPage() {
        $('.edit-page').addClass('hidden');
        $('.add-page').addClass('hidden');
        $('.save-page').removeClass('hidden');
        $('.cancel-page').removeClass('hidden');
        $('.form-group').removeClass('hidden');

        $('.page-title').addClass('hidden');
        $('.form-group').removeClass('hidden');

        $('#editor-title').val("New Page");
        $('#editor-path').val("/newpage.html");
        $('#editor-format').val("markdown");
        $('#editor-content').val("");

        $('#rendered-content').addClass('hidden');
        $('#page-editor').removeClass('hidden');
    }

    function savePage() {
        var format = $("#editor-format").val();
        var title = $('#editor-title').val();
        var content = $("#editor-content").val();
        var location = $('#editor-path').val();
        var path = location.replace(/\.html/, format);
        var uri = "/api" + path + "?title=" + title;

        console.info("PUT " + uri);

        sendRequest("PUT", uri, content, function() {
            if (this.status == 204) {
                console.info(this.status + " " + this.statusText);
                window.location.replace(location);
            } else {
                console.error(this.status + " " + this.statusText);
                console.error(this.responseText);
            }
        });
    }

    function cancelPage() {
        $('.save-page').addClass('hidden');
        $('.cancel-page').addClass('hidden');
        $('.edit-page').removeClass('hidden');
        $('.add-page').removeClass('hidden');

        $('.page-title').removeClass('hidden');
        $('.form-group').addClass('hidden');

        $('#page-editor').addClass('hidden');
        $('#rendered-content').removeClass('hidden');
    }

    function sendRequest(method, uri, body, callback) {
        var xhr = new XMLHttpRequest();
        xhr.open(method, uri, true);
        xhr.setRequestHeader("content-type", "text/plain");
        if (callback) {
            xhr.onload = callback;
        }
        xhr.send(body);
    }
})();
