var WIKIPEDIA_BASEURL="http://simple.m.wikipedia.org/wiki/";

function wikify() {
    var content = $('#text').val();
    $.ajax({
        url: "/wikifyHC/",
        method: "POST",
        data: {text: content}
    }).done((response) => {
        parseLinks(response);
    }).fail((jqXHR, msg) => {
        console.log("ERROR: " + msg);
    })
}

function linkClicked(targets, id) {
    $('#dialog').children("#dialog_links").attr('src', WIKIPEDIA_BASEURL + targets[id].documentId).parent()
        .dialog();
}

function parseLinks(links) {
    //map to array
    var sortedLinks = $.map(links, function(value, index) {
        return [value];
    });

    //sort by endPos desc
    sortedLinks.sort((arteezy, kuroky) => {
        return kuroky.entity.endPos - arteezy.entity.endPos;
    });

    let $content = $('#content');
    let newContent = $content.html();

    let i = 0;
    for(let link of sortedLinks) {
        let startPos = link.entity.startPos;
        let endPos = link.entity.endPos;
        let oldText = newContent.substr(startPos, endPos - startPos + 1);

        newContent = newContent.substr(0, startPos) + "<a id='link_" + i + "' href='#'>"
            + oldText + "</a>" + newContent.substr(endPos + 1);
        i++;
    }

    console.log(newContent);
    $content.html(newContent);

    for(let i = 0; i < sortedLinks.length; i++) {
        let link = sortedLinks[i];
        $('#link_' + i).click(() => {
            linkClicked(link.targets, 0);
        });
    }
}

$(document).ready(function() {
    $('#btn').click(() => {
        wikify();
    }).click();

});