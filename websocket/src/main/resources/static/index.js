$(function () {
    //设置联系对象
    let roomCode = $('#txtRoomNo').val();
    if (roomCode) {
        $('#contactPerson').text("正在房间号:" + roomCode + "通讯");
    }
    setTimeout(function () {
        $(window.frames["iframe_msg"].document).find("#msgList").append("<div>网络已连接成功...</div>")
    },1000)

})
//
let msgModel = {
    state: null,
    nick: null,
    msg: null,
    sendTime: null,
    roomCode: null,
    onlineMap: null,
    communicationObject: null,
    myCommunication: null,
    onLineList: [],
    msgList:[]

}

let onList = {
    onlineRooms: [],
    onlineUsers: []
};
// var server = 'ws://localhost:5018'; //如果开启了https则这里是wss
var server = 'ws://localhost:8089';

var WEB_SOCKET = new WebSocket(server + '/websocket');

WEB_SOCKET.onopen = function (evt) {
    console.log('Connection open ...');
    let msg = "网络连接已打开";
    msgModel.msg = msg

};

//收到消息
WEB_SOCKET.onmessage = function (evt) {

    if (evt.data) {
        let objcet = JSON.parse(evt.data);
        if (!objcet.state) {
            onList = JSON.parse(evt.data);
            let rooms = '';
            for (let i = 0; i < onList.onlineRooms.length;i++) {
                rooms += '<div><button onclick="sendRoomCode(&quot;'+ onList.onlineRooms[i] +'&quot;)" >房间号:'+ onList.onlineRooms[i] +'</button></div>';

            }
            let users = '';
            for (let key in onList.onlineUsers) {
                let myName = $('#txtNickName').val();
                let nick = onList.onlineUsers[key].nick;
                if (myName != nick) {
                    users += '<div><button onclick="sendNick(&quot;'+ key +'&quot;)">昵称:'+ nick +'</button</div>';
                }

            }
            $(window.frames["roomIframe"].document).find("#roomList").html(rooms);
            $(window.frames["userIframe"].document).find("#userList").html(users);

            console.log(onList.onlineUsers.length)
            console.log(onList)
        } else {
            msgModel = JSON.parse(evt.data);
            //获取实例对象的字符串
            var content = $('#msgList').val();
            let msg = "";

            console.log(msgModel)
            if (msgModel.state == "RETURN_NAME") {
                $('#txtNickName').val(msgModel.nick)
                $(window.frames["iframe_msg"].document).find("#msgList").append('<div>'+msgModel.msg+'...</div');

            } else if(msgModel.state == 'SEND_TO_ROOM') {
                let sendTime = "<text > 发送时间: " + msgModel.sendTime  +  "</text></br>"
                if (msgModel.self == true) {
                    console.log(msgModel.msgList)
                    msg = '<text >我:' + msgModel.msgList + '</text><HR>';
                    content = sendTime + msg;
                    $(window.frames["iframe_msg"].document).find("#msgList").append('<div style="float: right;clear:both;">'+content+'</div>');
                } else {
                    msg = '<text >' + msgModel.nick + ':' + msgModel.msg + '</text><HR>';
                    content = sendTime + msg;
                    $(window.frames["iframe_msg"].document).find("#msgList").append('<div style="float: left;clear:both;">'+content+'</div');

                }

            } else if(msgModel.state == 'JOIN') {

                $(window.frames["iframe_msg"].document).find("#msgList").append('<div>'+msgModel.msg+'...</div');

            } else if (msgModel.state == 'SEND_TO_NICK') {
                let sendTime = "<text > 发送时间: " + msgModel.sendTime  +  "</text></br>"
                let name = $('#txtNickName').val();
                if (name == msgModel.nick) {
                    msgModel.msgList = decrypt(msgModel.msgList)
                    msgModel.msg = utf8ByteToUnicodeStr(msgModel.msgList)
                    msg = '<text >我:' + msgModel.msg + '</text><HR>';
                    content = sendTime + msg;
                    $(window.frames["iframe_msg"].document).find("#msgList").append('<div style="float: right;clear:both;">'+content+'</div>');
                } else {

                    //对方显示
                    msgModel.msgList = decrypt(msgModel.msgList)
                    msgModel.msg = utf8ByteToUnicodeStr(msgModel.msgList)

                    msg = '<text >' + msgModel.nick + ':' + msgModel.msg + '</text><HR>';
                    content = sendTime + msg;
                    $('#contactPerson').text("正在与 " + msgModel.nick + " 通讯" )
                    $('#txtRoomNo').val("");
                    let nick = $('#txtNickName').val();
                    let myId = msgModel.communicationObject
                    let communicationObject = msgModel.myCommunication
                    msgModel.nick = nick;
                    msgModel.communicationObject = communicationObject;
                    msgModel.myCommunication = myId;
                    console.log(msgModel)
                    $(window.frames["iframe_msg"].document).find("#msgList").append('<div style="float: left;clear:both;">'+content+'</div');

                }
            }
        }


    }
};

WEB_SOCKET.onclose = function (evt) {
    console.log('连接已关闭.');
};
//加入房间
$('#btnJoin').on('click', function () {
    var roomNo = $('#txtRoomNo').val();
    var nick = $('#txtNickName').val();
    if (!nick || !roomNo) {
        alert("请填写完整昵称和房间号!");
        return;
    }
    if (roomNo) {
        msgModel.state = 'JOIN';
        msgModel.roomCode = roomNo;
        msgModel.nick = nick;
        WEB_SOCKET.send(JSON.stringify(msgModel));
    }
});
//发送消息
$('#btnSend').on('click', function () {
    console.log("发送消息")
    msgModel.msg = $('#txtMsg').val();
    msgModel.roomCode = $('#txtRoomNo').val();
    msgModel.nick = $('#txtNickName').val();
    let heName = onList.onlineUsers[msgModel.communicationObject]
    if (msgModel.roomCode) {
        msgModel.state = 'SEND_TO_ROOM';
        if (msgModel.msg) {
            msgModel.msgList = stringToByte(msgModel.msg)
            msgModel.msg = null;
            WEB_SOCKET.send(JSON.stringify(msgModel));
        }

        return;
    } else if (msgModel.nick) {
        if (heName) {
            //回复消息
            let replyMsg = {
                myCommunication: msgModel.communicationObject,
                communicationObject: msgModel.communicationObject,
                nick: heName,
            }

        }
        msgModel.state = 'SEND_TO_NICK';
        if (msgModel.msg) {
            msgModel.msgList = stringToByte(msgModel.msg)
            msgModel.msg = null;
            WEB_SOCKET.send(JSON.stringify(msgModel));
        }
    } else {
        alert("请填写完整昵称和房间号!");
    }

});

//退出房间
$('#btnLeave').on('click', function () {
    var nick = $('#txtNickName').val();
    var msg = {
        action: 'leave',
        msg: '',
        nick: nick
    };
    WEB_SOCKET.send(JSON.stringify(msg));
});

/**
 * 设置发送key
 * @param nick
 */
function setNick(nick) {
    console.log(nick)
    let name = onList.onlineUsers[nick].nick
    $('#txtRoomNo').val("");
    msgModel.communicationObject = nick;
    $('#contactPerson').text("正在与" + name + "通讯" )
    $(window.frames["iframe_msg"].document).find("#msgList").html("");


}
function setRoomCode(code) {
    console.log(code)
    $('#contactPerson').text("正在房间号:" + code + "通讯");
    $('#txtRoomNo').val(15);
}

function byteToString(arr) {
    if(typeof arr === 'string') {
        return arr;
    }
    var str = '',
        _arr = arr;
    for(var i = 0; i < _arr.length; i++) {
        var one = _arr[i].toString(2),
            v = one.match(/^1+?(?=0)/);
        if(v && one.length == 8) {
            var bytesLength = v[0].length;
            var store = _arr[i].toString(2).slice(7 - bytesLength);
            for(var st = 1; st < bytesLength; st++) {
                store += _arr[st + i].toString(2).slice(2);
            }
            str += String.fromCharCode(parseInt(store, 2));
            i += bytesLength - 1;
        } else {
            str += String.fromCharCode(_arr[i]);
        }
    }
    return str;
}

//字符串转字节序列
function stringToByte(str) {
    var bytes = new Array();
    var len, c;
    len = str.length;
    for(var i = 0; i < len; i++) {
        c = str.charCodeAt(i);
        if(c >= 0x010000 && c <= 0x10FFFF) {
            bytes.push(((c >> 18) & 0x07) | 0xF0);
            bytes.push(((c >> 12) & 0x3F) | 0x80);
            bytes.push(((c >> 6) & 0x3F) | 0x80);
            bytes.push((c & 0x3F) | 0x80);
        } else if(c >= 0x000800 && c <= 0x00FFFF) {
            bytes.push(((c >> 12) & 0x0F) | 0xE0);
            bytes.push(((c >> 6) & 0x3F) | 0x80);
            bytes.push((c & 0x3F) | 0x80);
        } else if(c >= 0x000080 && c <= 0x0007FF) {
            bytes.push(((c >> 6) & 0x1F) | 0xC0);
            bytes.push((c & 0x3F) | 0x80);
        } else {
            bytes.push(c & 0xFF);
        }
    }
    return bytes;


}

function utf8ByteToUnicodeStr(utf8Bytes){
    var unicodeStr ="";
    for (var pos = 0; pos < utf8Bytes.length;){
        var flag= utf8Bytes[pos];
        var unicode = 0 ;
        if ((flag >>>7) === 0 ) {
            unicodeStr+= String.fromCharCode(utf8Bytes[pos]);
            pos += 1;

        } else if ((flag &0xFC) === 0xFC ){
            unicode = (utf8Bytes[pos] & 0x3) << 30;
            unicode |= (utf8Bytes[pos+1] & 0x3F) << 24;
            unicode |= (utf8Bytes[pos+2] & 0x3F) << 18;
            unicode |= (utf8Bytes[pos+3] & 0x3F) << 12;
            unicode |= (utf8Bytes[pos+4] & 0x3F) << 6;
            unicode |= (utf8Bytes[pos+5] & 0x3F);
            unicodeStr+= String.fromCharCode(unicode) ;
            pos += 6;

        }else if ((flag &0xF8) === 0xF8 ){
            unicode = (utf8Bytes[pos] & 0x7) << 24;
            unicode |= (utf8Bytes[pos+1] & 0x3F) << 18;
            unicode |= (utf8Bytes[pos+2] & 0x3F) << 12;
            unicode |= (utf8Bytes[pos+3] & 0x3F) << 6;
            unicode |= (utf8Bytes[pos+4] & 0x3F);
            unicodeStr+= String.fromCharCode(unicode) ;
            pos += 5;

        } else if ((flag &0xF0) === 0xF0 ){
            unicode = (utf8Bytes[pos] & 0xF) << 18;
            unicode |= (utf8Bytes[pos+1] & 0x3F) << 12;
            unicode |= (utf8Bytes[pos+2] & 0x3F) << 6;
            unicode |= (utf8Bytes[pos+3] & 0x3F);
            unicodeStr+= String.fromCharCode(unicode) ;
            pos += 4;

        } else if ((flag &0xE0) === 0xE0 ){
            unicode = (utf8Bytes[pos] & 0x1F) << 12;;
            unicode |= (utf8Bytes[pos+1] & 0x3F) << 6;
            unicode |= (utf8Bytes[pos+2] & 0x3F);
            unicodeStr+= String.fromCharCode(unicode) ;
            pos += 3;

        } else if ((flag &0xC0) === 0xC0 ){ //110
            unicode = (utf8Bytes[pos] & 0x3F) << 6;
            unicode |= (utf8Bytes[pos+1] & 0x3F);
            unicodeStr+= String.fromCharCode(unicode) ;
            pos += 2;

        } else{
            unicodeStr+= String.fromCharCode(utf8Bytes[pos]);
            pos += 1;
        }
    }
    return unicodeStr;
}

function decrypt(arr) {
    for (let i=0;i<arr.length;i++) {
        arr[i] = (arr[i]  - 1889) /2;
    }
    return arr;
}