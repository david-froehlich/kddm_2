var WIKIPEDIA_BASEURL = "http://simple.m.wikipedia.org/wiki/";

var content;
function wikify() {
    content = $('#content').text();
    $.ajax({
        url: "/wikify/",
        method: "POST",
        contentType: 'text/plain',
        data: content
    }).done((response) => {
        parseLinks(response);
    }).fail((jqXHR, msg) => {
        console.log("ERROR: " + msg);
    })
}

function linkClicked(targets, id) {
    $('#dlg_iframe').attr('src', WIKIPEDIA_BASEURL + targets[id].documentId);

    let next_id = id < targets.length ? id + 1 : id;
    let prev_id = id > 0 ? id - 1 : 0;
    $('#dlg_prev_target').click(() => {
        linkClicked(targets, prev_id);
    });

    $('#dlg_next_target').click(() => {
        linkClicked(targets, next_id);
    });

    $('#dialog').show();
}

function parseLinks(links) {
    //map to array
    var sortedLinks = $.map(links, function (value, index) {
        return [value];
    });

    //sort by endPos desc
    sortedLinks.sort((arteezy, kuroky) => {
        return kuroky.entity.endPos - arteezy.entity.endPos;
    });

    let $content = $('#content');
    let newContent = content;

    let i = 0;
    for (let link of sortedLinks) {
        let startPos = link.entity.startPos;
        let endPos = link.entity.endPos;
        let oldText = newContent.substr(startPos, endPos - startPos);

        newContent = newContent.substr(0, startPos) + "<a id='link_" + i + "' href='#'>"
            + oldText + "</a>" + newContent.substr(endPos);
        i++;
    }

    $content.html(newContent);

    for (let i = 0; i < sortedLinks.length; i++) {
        let link = sortedLinks[i];
        $('#link_' + i).click(() => {
            linkClicked(link.targets, 0);
        });
    }
}

$(document).ready(function () {
    $('#btn').click(() => {
        wikify();
    });//.click();
    $('#dialog').hide();
    $('#dlg_close').click(() => {
        $('#dialog').hide();
    })
});