/// <reference path="../../typings/main.d.ts" />
(function () {
    "use strict";
    var Page = (function () {
        function Page() {
        }
        Page.prototype.init = function () {
            this.titleField = document.getElementById("editor-title");
            this.formatField = document.getElementById("editor-format");
            this.pathField = document.getElementById("editor-path");
            this.bodyField = document.getElementById("editor-content");
            this.sourcePathField = document.getElementById("source-path");
            this.pageTitle = document.getElementsByClassName("page-title");
            this.pageEditor = document.getElementById("page-editor");
            this.renderedBody = document.getElementById("rendered-content");
            this.formGroups = document.getElementsByClassName("form-group");
            this.addButtons = document.getElementsByClassName("add-page");
            this.editButtons = document.getElementsByClassName("edit-page");
            this.saveButtons = document.getElementsByClassName("save-page");
            this.cancleButtons = document.getElementsByClassName("cancel-page");
            for (var i = 0; i < this.editButtons.length; i++) {
                this.editButtons.item(i).addEventListener("click", function (e) {
                    e.preventDefault();
                    editPage();
                });
            }
            for (var i = 0; i < this.addButtons.length; i++) {
                this.addButtons.item(i).addEventListener("click", function (e) {
                    e.preventDefault();
                    addPage();
                });
            }
            for (var i = 0; i < this.saveButtons.length; i++) {
                this.saveButtons.item(i).addEventListener("click", function (e) {
                    e.preventDefault();
                    savePage();
                });
            }
            for (var i = 0; i < this.cancleButtons.length; i++) {
                this.cancleButtons.item(i).addEventListener("click", function (e) {
                    e.preventDefault();
                    cancelPage();
                });
            }
        };
        Page.prototype.hideEditor = function () {
            hideItem(this.titleField);
            hideItem(this.pathField);
            hideItem(this.formatField);
            hideItem(this.bodyField);
            hideItem(this.pageEditor);
            hideAll(this.saveButtons);
            hideAll(this.cancleButtons);
            hideAll(this.formGroups);
            showAll(this.addButtons);
            showAll(this.editButtons);
            showItem(this.renderedBody);
        };
        Page.prototype.showEditor = function (article) {
            hideAll(this.addButtons);
            hideAll(this.editButtons);
            hideItem(this.renderedBody);
            showItem(this.titleField);
            showItem(this.pathField);
            showItem(this.formatField);
            showItem(this.bodyField);
            showItem(this.pageEditor);
            showAll(this.saveButtons);
            showAll(this.cancleButtons);
            showAll(this.formGroups);
            this.titleField.value = article.title;
            this.pathField.value = article.path;
            this.formatField.value = article.format;
            this.bodyField.value = article.body;
            this.bodyField.selectionStart = 0;
            this.bodyField.selectionEnd = 0;
            this.bodyField.scrollTop = 0;
            this.bodyField.focus();
        };
        Page.prototype.createDocument = function () {
            return {
                title: this.titleField.value,
                path: this.pathField.value,
                format: this.formatField.value,
                body: this.bodyField.value
            };
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
        var path = page.sourcePathField.value;
        var uri = "" + path;
        console.info("GET " + uri);
        var headers = {
            "accept": "application/json"
        };
        sendRequest("GET", uri, null, 'json', headers, function (xhr) {
            if (xhr.status == 200) {
                console.info(xhr.status + "  " + xhr.statusText);
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
        page.showEditor({ title: "Untitled", path: "/untitled.json", format: "text/markdown", body: "" });
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