/// <reference path="../../typings/main.d.ts" />

(function () {
    "use strict";

    class SourceDocument
    {
        title: string;
        path: string;
        format: string;
        body: string;

        constructor(title: string, path: string, format: string, body: string)
        {
            this.title = title;
            this.path = path;
            this.format = format;
            this.body = body;
        }
    }

    class Page
    {
        titleInput : HTMLInputElement;
        formatInput: HTMLSelectElement;
        pathInput :  HTMLInputElement;
        contentInput : HTMLTextAreaElement;
        sourcePath : HTMLInputElement;

        pageEditorDiv : HTMLDivElement;
        renderedContentDiv : HTMLDivElement;
        formGroupDivs : NodeListOf<HTMLDivElement>;

        addPageButtons: NodeListOf<HTMLAnchorElement>;
        editPageButtons: NodeListOf<HTMLAnchorElement>;
        savePageButtons: NodeListOf<HTMLAnchorElement>;
        cancelPageButtons: NodeListOf<HTMLAnchorElement>;

        pageTitleElements : NodeListOf<HTMLElement>;

        init()
        {
            this.titleInput = <HTMLInputElement> document.getElementById("editor-title");
            this.formatInput = <HTMLSelectElement> document.getElementById("editor-format");
            this.pathInput = <HTMLInputElement> document.getElementById("editor-path");
            this.contentInput = <HTMLTextAreaElement> document.getElementById("editor-content");

            this.sourcePath = <HTMLInputElement> document.getElementById("source-path");
            this.pageTitleElements = <NodeListOf<HTMLElement>> document.getElementsByClassName("page-title");

            this.pageEditorDiv = <HTMLDivElement> document.getElementById("page-editor");
            this.renderedContentDiv = <HTMLDivElement> document.getElementById("rendered-content");
            this.formGroupDivs = <NodeListOf<HTMLDivElement>>document.getElementsByClassName("form-group");

            this.addPageButtons = <NodeListOf<HTMLAnchorElement>> document.getElementsByClassName("add-page");
            this.editPageButtons = <NodeListOf<HTMLAnchorElement>> document.getElementsByClassName("edit-page");
            this.savePageButtons = <NodeListOf<HTMLAnchorElement>> document.getElementsByClassName("save-page");
            this.cancelPageButtons = <NodeListOf<HTMLAnchorElement>> document.getElementsByClassName("cancel-page");

            for (let i = 0; i < this.editPageButtons.length; i++) {
                this.editPageButtons.item(i).addEventListener("click", function (e: MouseEvent) {
                    e.preventDefault();
                    editPage();
                })
            }
            for (let i = 0; i < this.addPageButtons.length; i++) {
                this.addPageButtons.item(i).addEventListener("click", function (e: MouseEvent) {
                    e.preventDefault();
                    addPage();
                })
            }
            for (let i = 0; i < this.savePageButtons.length; i++) {
                this.savePageButtons.item(i).addEventListener("click", function (e: MouseEvent) {
                    e.preventDefault();
                    savePage();
                })
            }
            for (let i = 0; i < this.cancelPageButtons.length; i++) {
                this.cancelPageButtons.item(i).addEventListener("click", function (e: MouseEvent) {
                    e.preventDefault();
                    cancelPage();
                })
            }
        }

        hideEditor()
        {
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
        }

        showEditor(doc: SourceDocument)
        {
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
        }

        createDocument()
        {
            return new SourceDocument(
                this.titleInput.value,
                this.pathInput.value,
                this.formatInput.value,
                this.contentInput.value);
        }
    }

    let page = new Page();

    hljs.initHighlightingOnLoad();

    document.onreadystatechange = function (e: ProgressEvent) : void
    {
        if ("complete" !== document.readyState) {
            return;
        }
        page.init();
    };

    function editPage() : void
    {
        let path: string = page.sourcePath.value;
        let uri: string = `${path}`;
        console.info(`GET ${uri}`);
        let headers = {
            "accept" : `application/json`
        };
        sendRequest("GET", uri, null, 'json', headers, function(xhr: XMLHttpRequest)
        {
            if (xhr.status == 200) {
                console.info(`${xhr.status}  ${xhr.statusText}`);
                console.info(xhr.response);
                let doc : SourceDocument = xhr.response;
                doc.path = path;
                page.showEditor(doc);
            } else {
                console.error(`${xhr.status}  ${xhr.statusText}`);
            }
        });
    }

    function addPage() : void
    {
        page.showEditor(new SourceDocument("New Page", "/newpage.json", "text/markdown", ""));
    }

    function savePage() : void
    {
        let doc = page.createDocument();
        let json = JSON.stringify(doc);
        let uri: string = `/api/content/${doc.path}`;

        console.info(`PUT ${uri}`);

        let headers = {
            "Content-Type" : "application/json"
        };

        sendRequest("PUT", uri, json, '', headers, function(xhr: XMLHttpRequest)
        {
            if (xhr.status == 204) {
                console.info(`${xhr.status}  ${xhr.statusText}`);
                window.location.replace(doc.path.replace(/\.json$/, ".html"));
            } else {
                console.error(`${xhr.status}  ${xhr.statusText}`);
                console.error(xhr.responseText);
            }
        });
    }

    function cancelPage() : void
    {
        page.hideEditor();
    }

    function addClass<E extends HTMLElement>(list: NodeListOf<E>, className: string)
    {
        each(list, e => e.classList.add(className));
    }

    function removeClass<E extends HTMLElement>(list: NodeListOf<E>, className: string)
    {
        each(list, e => e.classList.remove(className));
    }

    function each<N extends Node>(list: NodeListOf<N>, callback:(N) => any): void
    {
        for (let i = 0; i < list.length; i++) {
            callback(list[i]);
        }
    }

    function hideAll<E extends HTMLElement>(list: NodeListOf<E>)
    {
        addClass(list, 'hidden');
    }

    function showAll<E extends HTMLElement>(list: NodeListOf<E>)
    {
        removeClass(list, 'hidden');
    }

    function hideItem(element: HTMLElement)
    {
        element.classList.add('hidden');
    }

    function showItem(element: HTMLElement)
    {
        element.classList.remove('hidden');
    }

    function sendRequest(method: string, uri: string, body: any, responseType: string, headers: Object, callback:(xhr: XMLHttpRequest) => any) : void
    {
        let xhr = new XMLHttpRequest();
        xhr.responseType = responseType;
        xhr.open(method, uri, true);
        for (let name in headers) {
            xhr.setRequestHeader(name, headers[name]);
        }
        if (callback) {
            xhr.onload = function (e: Event) {
                callback(xhr);
            };
        }
        xhr.send(body);
    }
})();
