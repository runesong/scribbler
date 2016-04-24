/// <reference path="../../typings/main.d.ts" />

(function () {
    "use strict";

    interface Article
    {
        title: string;
        path: string;
        format: string;
        body: string;
    }

    class Page
    {
        titleField : HTMLInputElement;
        formatField: HTMLSelectElement;
        pathField :  HTMLInputElement;
        bodyField : HTMLTextAreaElement;
        sourcePathField : HTMLInputElement;

        pageEditor : HTMLDivElement;
        renderedBody : HTMLDivElement;
        formGroups : NodeListOf<HTMLDivElement>;

        addButtons: NodeListOf<HTMLAnchorElement>;
        editButtons: NodeListOf<HTMLAnchorElement>;
        saveButtons: NodeListOf<HTMLAnchorElement>;
        cancleButtons: NodeListOf<HTMLAnchorElement>;

        pageTitle : NodeListOf<HTMLElement>;

        init() : void
        {
            this.titleField  = <HTMLInputElement> document.getElementById("editor-title");
            this.formatField = <HTMLSelectElement> document.getElementById("editor-format");
            this.pathField   = <HTMLInputElement> document.getElementById("editor-path");
            this.bodyField   = <HTMLTextAreaElement> document.getElementById("editor-content");
            this.sourcePathField = <HTMLInputElement> document.getElementById("source-path");

            this.pageTitle    = <NodeListOf<HTMLElement>> document.getElementsByClassName("page-title");
            this.pageEditor   = <HTMLDivElement> document.getElementById("page-editor");
            this.renderedBody = <HTMLDivElement> document.getElementById("rendered-content");
            this.formGroups   = <NodeListOf<HTMLDivElement>>document.getElementsByClassName("form-group");

            this.addButtons    = <NodeListOf<HTMLAnchorElement>> document.getElementsByClassName("add-page");
            this.editButtons   = <NodeListOf<HTMLAnchorElement>> document.getElementsByClassName("edit-page");
            this.saveButtons   = <NodeListOf<HTMLAnchorElement>> document.getElementsByClassName("save-page");
            this.cancleButtons = <NodeListOf<HTMLAnchorElement>> document.getElementsByClassName("cancel-page");

            for (let i = 0; i < this.editButtons.length; i++) {
                this.editButtons.item(i).addEventListener("click", function (e: MouseEvent) {
                    e.preventDefault();
                    editPage();
                })
            }
            for (let i = 0; i < this.addButtons.length; i++) {
                this.addButtons.item(i).addEventListener("click", function (e: MouseEvent) {
                    e.preventDefault();
                    addPage();
                })
            }
            for (let i = 0; i < this.saveButtons.length; i++) {
                this.saveButtons.item(i).addEventListener("click", function (e: MouseEvent) {
                    e.preventDefault();
                    savePage();
                })
            }
            for (let i = 0; i < this.cancleButtons.length; i++) {
                this.cancleButtons.item(i).addEventListener("click", function (e: MouseEvent) {
                    e.preventDefault();
                    cancelPage();
                })
            }
        }

        hideEditor() : void
        {
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
        }

        showEditor(article: Article) : void
        {
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
        }

        createDocument() : Article
        {
            return {
                title: this.titleField.value,
                path: this.pathField.value,
                format: this.formatField.value,
                body: this.bodyField.value
            }
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
        let path: string = page.sourcePathField.value;
        let uri: string = `${path}`;
        console.info(`GET ${uri}`);
        let headers = {
            "accept" : `application/json`
        };
        sendRequest("GET", uri, null, 'json', headers, function(xhr: XMLHttpRequest)
        {
            if (xhr.status == 200) {
                console.info(`${xhr.status}  ${xhr.statusText}`);
                let doc : Article = xhr.response;
                doc.path = path;
                page.showEditor(doc);
            } else {
                console.error(`${xhr.status}  ${xhr.statusText}`);
            }
        });
    }

    function addPage() : void
    {
        page.showEditor({ title: "Untitled", path: "/untitled.json", format: "text/markdown", body: ""});
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
