(function () {
    "use strict";

    hljs.initHighlightingOnLoad();

    document.onreadystatechange = function (e)
    {
        if ("complete" !== document.readyState) {
            return;
        }
        initialize();
    };

    function initialize()
    {
        $(".edit-page").click(function () { editPage(); });
        $(".add-page").click(function() { addPage(); });
        $(".save-page").click(function() { savePage(); });
        $(".cancel-page").click(function() { cancelPage(); });
    }

    function editPage()
    {
        var format = $('#source-format').val();
        var path = $('#source-path').val();
        var uri = "/api/content" + path;
        console.info("GET " + uri);
        sendRequest("GET", uri, null, format, function()
        {
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
                $('#editor-content').each(function ()
                {
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

    function addPage()
    {
        $('.edit-page').addClass('hidden');
        $('.add-page').addClass('hidden');
        $('.save-page').removeClass('hidden');
        $('.cancel-page').removeClass('hidden');
        $('.form-group').removeClass('hidden');

        $('.page-title').addClass('hidden');
        $('.form-group').removeClass('hidden');

        $('#editor-title').val("New Page");
        $('#editor-path').val("/newpage.html");
        $('#editor-format').val("text/markdown");
        $('#editor-content').val("");

        $('#rendered-content').addClass('hidden');
        $('#page-editor').removeClass('hidden');
    }

    function savePage()
    {
        var format = $("#editor-format").val();
        var title = $('#editor-title').val();
        var content = $("#editor-content").val();
        var path = $('#editor-path').val();
        var uri = "/api/content" + path + "?title=" + title;

        console.info("PUT " + uri);

        var headers = {
            "content-type" : format
        };

        sendRequest("PUT", uri, content, headers, function()
        {
            if (this.status == 204) {
                console.info(this.status + " " + this.statusText);
                window.location.replace(path.replace(/\.\w+$/, ".html"));
                alert("done");
            } else {
                console.error(this.status + " " + this.statusText);
                console.error(this.responseText);
            }
        });
    }

    function cancelPage()
    {
        $('.save-page').addClass('hidden');
        $('.cancel-page').addClass('hidden');
        $('.edit-page').removeClass('hidden');
        $('.add-page').removeClass('hidden');

        $('.page-title').removeClass('hidden');
        $('.form-group').addClass('hidden');

        $('#page-editor').addClass('hidden');
        $('#rendered-content').removeClass('hidden');
    }

    function sendRequest(method, uri, body, headers, callback)
    {
        var xhr = new XMLHttpRequest();
        xhr.open(method, uri, true);
        for (var name in headers) {
            xhr.setRequestHeader(name, headers[name]);
        }
        if (callback) {
            xhr.onload = callback;
        }
        xhr.send(body);
    }
})();
