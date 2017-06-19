var WIKIPEDIA_BASEURL = "http://simple.m.wikipedia.org/wiki/";

var content;
function wikify() {
    content = $('#content').text();
    $.ajax({
        url: "/wikify/",
        method: "POST",
        contentType: 'text/plain',
        data: JSON.stringify({
            text: content,
            algorithmId: $('input[name=algorithm]:checked').val(),
            weightRatio: $('#weight_ratio').val(),
            linkRatio: $('#link_ratio').val()
        }),
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        },
        dataType: 'json'
    }).done((response) => {
        parseLinks(response);
    }).fail((jqXHR, msg) => {
        console.log("ERROR: " + msg);
    })
}

function linkClicked(targets, id, entity_weight, top_doc_relevance) {
    $('#dlg_iframe').attr('src', WIKIPEDIA_BASEURL + targets[id].documentId);

    let next_id = id < targets.length ? id + 1 : id;
    let prev_id = id > 0 ? id - 1 : 0;
    $('#dlg_prev_target').click(() => {
        linkClicked(targets, prev_id);
    });

    $('#dlg_next_target').click(() => {
        linkClicked(targets, next_id);
    });

    $('#top_doc_relevance').val(top_doc_relevance);
    $('#entity_weight').val(entity_weight);

    $('#dialog').show();
}

function parseLinks(links) {
    //map to array
    let sortedLinks = $.map(links, function (value, index) {
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
            linkClicked(link.targets, 0, link.entity.weight, link.targets[0].relevance);
        });
    }
}

function refreshIndexingStatus() {
    $.ajax({
        type: 'GET',
        url: '/indexing/',
        dataType: 'json',
        headers: {
            'Accept': 'application/json',
        }
    }).done(function (data) {
        $('#index_status').text(data['indexIsValid'] ? 'valid' : 'invalid');
        $('#indexing_progress').text(data['numProcessedPages']);
        $('#indexing_running').text(data['isRunning'] ? 'yes' : 'no');
        $('#index_num_docs').text(data['numDocumentsInIndex']);
        $('#start_indexing_btn').prop('disabled', data['isRunning']);
    }).fail(function (err) {
        console.error(err);
    }).always(function () {
        setTimeout(refreshIndexingStatus, 2000);
    });
}

function startIndexing() {
    $.ajax({
        type: 'GET',
        url: '/indexing/start',
    }).done(function (data) {
        console.log(data);
        $('#start_indexing_btn').prop('disabled', true);
    }).fail(function (err) {
        console.error(err);
    }).always(function () {
    });
}

$(document).ready(function () {
    $('#btn').click(() => {
        wikify();
    });//.click();
    $('#dialog').hide();
    $('#dlg_close').click(() => {
        $('#dialog').hide();
    });

    $('#start_indexing_btn').click(startIndexing);
    refreshIndexingStatus();
});