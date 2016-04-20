/// <reference path="../../typings/main.d.ts" />
(function () {
    "use strict";
    var SourceDocument = (function () {
        function SourceDocument(title, path, format, body) {
            this.title = title;
            this.path = path;
            this.format = format;
            this.body = body;
        }
        return SourceDocument;
    }());
    var Page = (function () {
        function Page() {
        }
        Page.prototype.init = function () {
            this.titleInput = document.getElementById("editor-title");
            this.formatInput = document.getElementById("editor-format");
            this.pathInput = document.getElementById("editor-path");
            this.contentInput = document.getElementById("editor-content");
            this.sourcePath = document.getElementById("source-path");
            this.pageTitleElements = document.getElementsByClassName("page-title");
            this.pageEditorDiv = document.getElementById("page-editor");
            this.renderedContentDiv = document.getElementById("rendered-content");
            this.formGroupDivs = document.getElementsByClassName("form-group");
            this.addPageButtons = document.getElementsByClassName("add-page");
            this.editPageButtons = document.getElementsByClassName("edit-page");
            this.savePageButtons = document.getElementsByClassName("save-page");
            this.cancelPageButtons = document.getElementsByClassName("cancel-page");
            for (var i = 0; i < this.editPageButtons.length; i++) {
                this.editPageButtons.item(i).addEventListener("click", function (e) {
                    e.preventDefault();
                    editPage();
                });
            }
            for (var i = 0; i < this.addPageButtons.length; i++) {
                this.addPageButtons.item(i).addEventListener("click", function (e) {
                    e.preventDefault();
                    addPage();
                });
            }
            for (var i = 0; i < this.savePageButtons.length; i++) {
                this.savePageButtons.item(i).addEventListener("click", function (e) {
                    e.preventDefault();
                    savePage();
                });
            }
            for (var i = 0; i < this.cancelPageButtons.length; i++) {
                this.cancelPageButtons.item(i).addEventListener("click", function (e) {
                    e.preventDefault();
                    cancelPage();
                });
            }
        };
        Page.prototype.hideEditor = function () {
            hideItem(this.titleInput);
            hideItem(this.pathInput);
            hideItem(this.formatInput);
            hideItem(this.contentInput);
            hideItem(this.pageEditorDiv);
            hideAll(this.savePageButtons);
            hideAll(this.cancelPageButtons);
            hideAll(this.formGroupDivs);
            showAll(this.addPageButtons);
            showAll(this.editPageButtons);
            showItem(this.renderedContentDiv);
        };
        Page.prototype.showEditor = function (doc) {
            hideAll(this.addPageButtons);
            hideAll(this.editPageButtons);
            hideItem(this.renderedContentDiv);
            showItem(this.titleInput);
            showItem(this.pathInput);
            showItem(this.formatInput);
            showItem(this.contentInput);
            showItem(this.pageEditorDiv);
            showAll(this.savePageButtons);
            showAll(this.cancelPageButtons);
            showAll(this.formGroupDivs);
            this.titleInput.value = doc.title;
            this.pathInput.value = doc.path;
            this.formatInput.value = doc.format;
            this.contentInput.value = doc.body;
            this.contentInput.selectionStart = 0;
            this.contentInput.selectionEnd = 0;
            this.contentInput.scrollTop = 0;
            this.contentInput.focus();
        };
        Page.prototype.createDocument = function () {
            return new SourceDocument(this.titleInput.value, this.pathInput.value, this.formatInput.value, this.contentInput.value);
        };
        return Page;
    }());
    var page = new Page();
    hljs.initHighlightingOnLoad();
    document.onreadystatechange = function (e) {
        if ("complete" !== document.readyState) {
            return;
        }
        page.init();
    };
    function editPage() {
        var path = page.sourcePath.value;
        var uri = "" + path;
        console.info("GET " + uri);
        var headers = {
            "accept": "application/json"
        };
        sendRequest("GET", uri, null, 'json', headers, function (xhr) {
            if (xhr.status == 200) {
                console.info(xhr.status + "  " + xhr.statusText);
                console.info(xhr.response);
                var doc = xhr.response;
                doc.path = path;
                page.showEditor(doc);
            }
            else {
                console.error(xhr.status + "  " + xhr.statusText);
            }
        });
    }
    function addPage() {
        page.showEditor(new SourceDocument("New Page", "/newpage.json", "text/markdown", ""));
    }
    function savePage() {
        var doc = page.createDocument();
        var json = JSON.stringify(doc);
        var uri = "/api/content/" + doc.path;
        console.info("PUT " + uri);
        var headers = {
            "Content-Type": "application/json"
        };
        sendRequest("PUT", uri, json, '', headers, function (xhr) {
            if (xhr.status == 204) {
                console.info(xhr.status + "  " + xhr.statusText);
                window.location.replace(doc.path.replace(/\.json$/, ".html"));
            }
            else {
                console.error(xhr.status + "  " + xhr.statusText);
                console.error(xhr.responseText);
            }
        });
    }
    function cancelPage() {
        page.hideEditor();
    }
    function addClass(list, className) {
        each(list, function (e) { return e.classList.add(className); });
    }
    function removeClass(list, className) {
        each(list, function (e) { return e.classList.remove(className); });
    }
    function each(list, callback) {
        for (var i = 0; i < list.length; i++) {
            callback(list[i]);
        }
    }
    function hideAll(list) {
        addClass(list, 'hidden');
    }
    function showAll(list) {
        removeClass(list, 'hidden');
    }
    function hideItem(element) {
        element.classList.add('hidden');
    }
    function showItem(element) {
        element.classList.remove('hidden');
    }
    function sendRequest(method, uri, body, responseType, headers, callback) {
        var xhr = new XMLHttpRequest();
        xhr.responseType = responseType;
        xhr.open(method, uri, true);
        for (var name_1 in headers) {
            xhr.setRequestHeader(name_1, headers[name_1]);
        }
        if (callback) {
            xhr.onload = function (e) {
                callback(xhr);
            };
        }
        xhr.send(body);
    }
})();
//# sourceMappingURL=app.js.map