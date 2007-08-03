function setup() {
    var deltas = getElementsByClass(document, 'DIV', 'delta');
    for (var i = 0; i < deltas.length; i++) {
        var delta = deltas[i];
        // hide lists
        switchDetails(delta);

        // add links
        var linkA = document.createElement('A');
        linkA.href = '#';
        linkA.onclick = createSwitchDetailsOnClickFunction(delta);
        linkA.appendChild(document.createTextNode('(show details)'));
        linkA.className = 'deltaDetailsLink';
        deltas[i].getElementsByTagName('H3')[0].appendChild(linkA);
    }
}

function getElementsByClass(parent, tagName, className) {
    var result = new Array();
    var elements = parent.getElementsByTagName(tagName);
    for (var i = 0, j = 0; i < elements.length; i++) {
        if (elements[i].className == className) {
            result[j] = elements[i];
            j++;
        }
    }
    return result;
}

function createSwitchDetailsOnClickFunction(deltaDiv) {
    return function() {
        switchDetails(deltaDiv);
        return false;
    };
}

function switchDetails(deltaDiv) {
    var nowVisible;
    var lists = deltaDiv.getElementsByTagName('UL');
    for (var i = 0; i < lists.length; i++) {
        if (lists[i].style.display == 'none') {
            lists[i].style.display = '';
            nowVisible = true;
        } else {
            lists[i].style.display = 'none';
            nowVisible = false;
        }
    }
    var links = getElementsByClass(deltaDiv, 'A', 'deltaDetailsLink');
    for (var i = 0; i < links.length; i++) {
        var link = links[i];
        var newText = document.createTextNode(nowVisible ? '(hide details)' : '(show details)');
        link.replaceChild(newText, link.firstChild);
    }
}

window.onload = setup;
