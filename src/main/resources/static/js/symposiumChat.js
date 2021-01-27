function scrollBottom(){
	var scrollBottom =$('#talk-area').mCustomScrollbar('scrollTo', 'bottom', {
						scrollInertia : 0
					});
	return scrollBottom;
}
function scrollTop(){
	/*var scrollTop =$('#mo-talk-area').mCustomScrollbar('scrollTo', 'top', {
						scrollInertia : 0
					});
	return scrollTop;*/
}
function sendApprove(obj,idx){
		var approvBtn = $(obj).parent().parent().parent().find(":Button:eq(0)");
		$(approvBtn).click();
		$(obj).remove();

		$.ajax({
			method : "GET",
			url : "/chatting/updateMessageType",
			contentType : "applcation/json; charset=UTF-8",
			data: {
				idx : idx
			}
		}).done(function(){

		});
}
//refresh after approve message
function beforesendApprove(obj,idx,userIdx,userId,name,msg){
	var approvBtn = $(obj).parent().parent().parent().find(":Button:eq(0)");
	approvBtn.parent("li").removeClass("me");
	approvBtn.parent("li").addClass("approve");
	ChatClient.approveMessage(userIdx, userId, name, msg);
	$(obj).remove(); //this
	$.ajax({
		method : "GET",
		url : "/chatting/updateMessageType",
		contentType : "application/json; charset=UTF-8",
		data: {
			idx : idx
		}
	}).done(function(){

	});
}
function speakerSendMessage(obj){
	var speakerSendMessage = "<p>"+$(obj).parent().parent().parent().find(".txt").html()+"</p>";
	oEditors.getById["ir1"].exec("PASTE_HTML", [speakerSendMessage]); //"UPDATE_CONTENTS_FIELD", []
}

function removeBlackList(obj,userIdx){
//	console.log("removeBlackList 클릭");
	ChatClient.removeBlackList(userIdx, function(data){
		$(obj).parent().parent("li").remove();
		$.ajax({
			method : "GET",
			url : "/chatting/removeBlackUser",
			contentType : "application/json; charset=UTF-8",
			data : {
				idx : userIdx
			}
		}).done(function(data){
		});
		alert("차단 해제");
		$(".black"+userIdx).each(function(){
			$(this).css("display","inline");
		});
	});
}
function addBlackList(obj,userIdx,userId,name){
	console.log("addBlackList 클릭" );
	ChatClient.addBlackList(userIdx, function() {//<span>"+userId.substring(0,(userId.length)-(name.length+1))+"</span>
		$("#scroll_area").append("<li><span style='width:66%';>"+name+"</span><span><a href='javascript:void(0)' onclick='removeBlackList("+"this,"+userIdx+");'>해지</a></span></li>");

		$(".black"+userIdx).each(function(index){
			$(this).css("display","none");
		});
	});
}
var ChatView = (function() {
	var isAdmin = false;
    var room_names = new Array() ;

    var userInfo = {
			userMessageIdx : -1,
			chatRoomType : -1,
			toUserId : ''
		};

    var chatNotice = {
	    noticeTextAdmin : '',
	    noticeTextMe : '',
	    fromText : ''
    };

	var room_name_exists = function( roomname) {
                console.log("room_name_exists called");
                var output = false
                var BreakException = {};
//                console.log( output );
		ChatClient.getChatRoomList(function (data) {
			//$('#chat-room').empty();
			if (data && data.length !== 0) {
                try {
				data.forEach ( function(room) {
					if (room.description) {
						description = room.description;
					}
					//var tr = $('<tr>', {
						//id: "ROOM_" + room.name
					//}).append('<td>' + room.name + '</td><td>' + room.userName + '</td><td>' + room.description +'</td>');
					//$('#chat-room').append(tr);
                    console.log(roomname, room.name, output);
                    if( roomname == room.name ) {
                        output = true;
                        console.log("Updateed value");
                        console.log(output);
                        throw BreakException;
                         }
				}
                ); }
                    catch (e) { if (e !== BreakException) throw e; }
			};
		});
        console.log("room_name_exists Returning value");
        console.log(output);
        return output;
                    //console.log( room_names );

	};

	var addUserToUserList = function(userIdx, userId, userName) {
		var optionCount = $(".adminToUser").size();
		var newUser = "";

		if(optionCount ==0 ){
				newUser = $('<select>', {
					'class': 'adminToUser',
					'id' : 'selectUser'
				});
			var newUserDiv = $('<option>',{
					'data-user': 'admin',
					'value' : 'admin',
					'style' : 'padding:0px;'
			//}).append(userId + '(' + userName + ')');
			}).append('전체 보내기');
			newUser.append(newUserDiv);
			$('#user-list').append(newUser);
		}
		if(userIdx!=1){
			$(".adminToUser").append("<option id='USER_"+userIdx+"' value='"+userIdx+"'>"+userId+"</option>");
		}
	};
	var getUserList = function() {
		ChatClient.getUserList(function(data) {
			$('#user-list').empty();
			if (data && data.length !== 0) {
				data.forEach (function(user) {
					addUserToUserList(user.userIdx, user.userId, user.userName);
				});
			}
		});
	};

	var enterChatRoom = function(programIdx, name, adminIdx) {
		if (programIdx !== -1) {
			$('#room-name').html(name);
			$('#chatting-room').show();

			if (isAdmin === true) {
				$('#room-name').removeClass('col-lg-10').addClass('col-lg-8');
				$('#chatroom-admin-btn').show();
			} else {
				$('#room-name').removeClass('col-lg-8').addClass('col-lg-10')
				$('#chatroom-admin-btn').hide();
			}
			getUserList();
		}
	};

	var exitChatRoom = function(async) {
		//console.log("exitChatRoom 들어옴");
		ChatClient.exitChatRoom(async, function() {
			roomId = -1;
			$('#user-list').empty();
			$('#chat-messages').empty();
			$('#chatting-room').hide();
		});
	};
	var processEvents = function(events) {
		if (events && events.length > 0) {
			events.forEach (function(event) {
				switch(event.type) {
				case EventType.NORMAL_MSG:
					//오픈형
					if(userInfo.chatRoomType===0){
						if(userInfo.userMessageIdx == 1 && event.fromUserIdx==userInfo.userMessageIdx ){
							$('#chat-messages').append('<li class="admin"><div class="clear"><p class="name fl">'+chatNotice.fromText+'</p>'+
									'<div class="fr">'+
									'</div></div><div class="cont"><span class="bg_top"></span><p class="txt">'+event.msg+'</p>'+
									'<span class="bg_bottom"></span></div></li>');
							scrollBottom();
						}else if(userInfo.userMessageIdx == 1 && event.fromUserIdx!=userInfo.userMessageIdx && event.name != 'admin'){
							$('#chat-messages').append('<li class="approve"><p class="name"><div class="clear">'+event.name+'<a href="javascript:void(0)" class="black'+event.fromUserIdx+'" onclick="addBlackList(this,'+event.from_userIdx+',\''+event.userId+'\',\''+event.name+'\')"><img src="/img/admin/ico_caution.png" alt="" /></a>'
									 +'<div class="fr"><a href="javascript:void(0)" onclick="speakerSendMessage(this);"><img src="/img/admin/ico_t2.png" alt="" /></a><a href="javascript:void(0)" onclick="goDirectMessage('+event.from_userIdx+',\''+event.userId+'\')"><img src="/img/admin/ico_t4.png" alt="" /></a>'+
										'</div></div></p><div class="cont"><span class="bg_top"></span><p class="txt">'+event.msg+'</p><span class="bg_bottom"></span>'+
										'</div></li>');
							scrollBottom();
						}else if(userInfo.userMessageIdx != 1 && event.fromUserIdx ==userInfo.userMessageIdx ){
							$("#chat-messages").append('<li class="me"><p class="name">'+event.name+'</p><div class="cont">'
									+'<span class="bg_top"></span><p class="txt">'+event.msg+'</p><span class="bg_bottom"></span>'+
									'</div></li>');
							scrollBottom();
						}else if(userInfo.userMessageIdx != 1 && event.fromUserIdx!=userInfo.userMessageIdx &&  event.name == 'admin'){
							$('#chat-messages').append('<li class="admin"><div class="clear"><p class="name fl">'+chatNotice.fromText+'</p>'+
									'<div class="fr"></div></div><div class="cont"><span class="bg_top"></span><p class="txt">'+event.msg+'</p>'+
									'<span class="bg_bottom"></span></div></li>');
							scrollBottom();
						}else{
							$('#chat-messages').append('<li class="user"><p class="name"><div class="clear">'+event.name+''
									 +'<div class="fr">'+
										'</div></div></p><div class="cont"><span class="bg_top"></span><p class="txt">'+event.msg+'</p><span class="bg_bottom"></span>'+
										'</div></li>');
							scrollBottom();
						}

						//모바일
						if(userInfo.userMessageIdx == 1 && event.fromUserIdx==userInfo.userMessageIdx ){
							$('#mo-chat-messages').prepend('<li class="admin"><div class="clear"><p class="name fl">'+chatNotice.fromText+'</p>'+
									'<div class="fr">'+
									'</div></div><div class="cont"><span class="bg_top"></span><p class="txt">'+event.msg+'</p>'+
									'<span class="bg_bottom"></span></div></li>');
						}else if(userInfo.userMessageIdx == 1 && event.fromUserIdx!=userInfo.userMessageIdx && event.name != 'admin'){
							$('#mo-chat-messages').prepend('<li class="me"><p class="name"><div class="clear">'+event.name+'<a href="javascript:void(0)" onclick="addBlackList(this,'+event.from_userIdx+',\''+event.userId+'\',\''+event.name+'\')">'
									 +'<div class="fr"><a href="javascript:void(0)" onclick="speakerSendMessage(this);"><img src="/img/admin/ico_t2.png" alt="" /></a>'+
										'</div></div></p><div class="cont"><span class="bg_top"></span><p class="txt">'+event.msg+'한6</p><span class="bg_bottom"></span>'+
										'</div></li>');
						}else if(userInfo.userMessageIdx != 1 && event.fromUserIdx ==userInfo.userMessageIdx ){
							$("#mo-chat-messages").prepend('<li class="me"><p class="name">'+event.name+'</p><div class="cont">'
									+'<span class="bg_top"></span><p class="txt">'+event.msg+'</p><span class="bg_bottom"></span>'+
									'</div></li>');
							scrollTop();
						}else if(userInfo.userMessageIdx != 1 && event.fromUserIdx!=userInfo.userMessageIdx &&  event.name == 'admin'){
							$('#mo-chat-messages').prepend('<li class="admin"><div class="clear"><p class="name fl">'+chatNotice.fromText+'</p>'+
									'<div class="fr"></div></div><div class="cont"><span class="bg_top"></span><p class="txt">'+event.msg+'</p>'+
									'<span class="bg_bottom"></span></div></li>');
							scrollTop();
						}else{
							$('#mo-chat-messages').prepend('<li class="user"><p class="name"><div class="clear">'+event.name+''
									 +'<div class="fr">'+
										'</div></div></p><div class="cont"><span class="bg_top"></span><p class="txt">'+event.msg+'</p><span class="bg_bottom"></span>'+
										'</div></li>');
							scrollTop();
						}

					}//if
					else if(userInfo.chatRoomType===1){
						//console.log(" event.fromUserIdx : " + event.fromUserIdx + "userMessageIdx : " + userInfo.userMessageIdx +
						//		"event.name : " +event.name  );
						//관리자 화면 admin 일때 자기 자신 아이콘 안보임 (웹적용)
						if(event.fromUserIdx==userInfo.userMessageIdx && event.name == 'admin'){
							$('#chat-messages').append('<li class="admin"><div class="clear"><p class="name fl">'+"Admin"/*event.name*/+'</p>'+
									'<div class="fr">'+
									'</div></div><div class="cont"><span class="bg_top"></span><p class="txt">'+event.msg+'</p>'+
									'<span class="bg_bottom"></span></div></li>');
						}else if(event.fromUserIdx!=userInfo.userMessageIdx && event.name == 'admin'){
							$('#chat-messages').append('<li class="admin"><div class="clear"><p class="name fl">'+"Admin"/*event.name*/+'</p>'+
									'<div class="fr"></div></div><div class="cont"><span class="bg_top"></span><p class="txt">'+event.msg+'</p>'+
									'<span class="bg_bottom"></span></div></li>');
						}else if(event.fromUserIdx!=userInfo.userMessageIdx && event.name != 'admin'){
							$('#chat-messages').append('<li class="me"><p class="name"><div class="clear">'+event.name+'<a href="javascript:void(0)" onclick="addBlackList(this,'+event.from_userIdx+',\''+event.userId+'\',\''+event.name+'\')"><img src="/img/admin/ico_caution.png" alt="" /></a>'
									 +'<div class="fr"><a href="#"><img src="/img/admin/ico_t2.png" alt="" /></a><a href="#"><img src="/img/admin/ico_t3.png" alt="" /></a>'+
										'</div></div></p><div class="cont"><span class="bg_top"></span><p class="txt">'+event.msg+'한7</p><span class="bg_bottom"></span>'+
										'</div></li>');
						}else{
							$("#chat-messages").append('<li class="me"><p class="name">'+event.name+'</p><div class="cont">'
									+'<span class="bg_top"></span><p class="txt">'+event.msg+'</p><span class="bg_bottom"></span>'+
									'</div></li>');
						}
						//모바일
						//var message_old = $('#mo-chat-messages').html();
						if(event.fromUserIdx==userInfo.userMessageIdx && event.name == 'admin'){
							$('#mo-chat-messages').append( '<li class="admin"><div class="clear"><p class="name fl">'+event.name+'</p>'+
									'<div class="fr">'+
									'</div></div><div class="cont"><span class="bg_top"></span><p class="txt">'+event.msg+'</p>'+
									'<span class="bg_bottom"></span></div></li>'/*+ message_old*/);
						}else if(event.fromUserIdx!=userInfo.userMessageIdx && event.name == 'admin'){
							$('#mo-chat-messages').append('<li class="admin"><div class="clear"><p class="name fl">'+event.name+'</p>'+
									'<div class="fr"></div></div><div class="cont"><span class="bg_top"></span><p class="txt">'+event.msg+'</p>'+
									'<span class="bg_bottom"></span></div></li>');
						}else if(event.fromUserIdx!=userInfo.userMessageIdx && event.name != 'admin'){
							$('#mo-chat-messages').append('<li class="me"><p class="name"><div class="clear">'+event.name+'<a href="javascript:void(0)" onclick="addBlackList(this,'+event.from_userIdx+',\''+event.userId+'\',\''+event.name+'\')"><img src="/img/admin/ico_caution.png" alt="" /></a>'
									 +'<div class="fr"><a href="#"><img src="/img/admin/ico_t2.png" alt="" /></a><a href="#"><img src="/img/admin/ico_t3.png" alt="" /></a>'+
										'</div></div></p><div class="cont"><span class="bg_top"></span><p class="txt">'+event.msg+'한8</p><span class="bg_bottom"></span>'+
										'</div></li>');
						}else{
							$("#mo-chat-messages").append('<li class="me"><p class="name">'+event.name+'</p><div class="cont">'
									+'<span class="bg_top"></span><p class="txt">'+event.msg+'</p><span class="bg_bottom"></span>'+
									'</div></li>');
						}
					//승인형
					}else if(userInfo.chatRoomType===2){
						//관리자가 보내고 관리자가 받을 때 //전체말
						if(userInfo.userMessageIdx == 1 && event.fromUserIdx==userInfo.userMessageIdx ){
							$('#chat-messages').append('<li class="admin"><div class="clear"><p class="name fl">'+chatNotice.fromText+'</p>'+
									'<div class="fr">'+
									'</div></div><div class="cont"><span class="bg_top"></span><p class="txt">'+event.msg+'</p>'+
									'<span class="bg_bottom"></span></div></li>');
							scrollBottom();
						}else if(userInfo.userMessageIdx == 1 && event.fromUserIdx!=userInfo.userMessageIdx && event.name != 'admin'){
							//관리자가 승인 후 배경이 회색에서 흰색이 되기 때문에 굳이 메시지를 한 번더 표현 할 필요 없음;
							/*$('#chat-messages').append('<li class="me"><p class="name"><div class="clear">'+event.name+'<a href="#"><img src="/img/admin/ico_caution.png" alt="" /></a>'
									 +'<div class="fr"><a href="#"><img src="/img/admin/ico_t2.png" alt="" /></a><a href="#"><img src="/img/admin/ico_t3.png" alt="" /></a>'+
										'</div></div></p><div class="cont"><span class="bg_top"></span><p class="txt">'+event.msg+'</p><span class="bg_bottom"></span>'+
										'</div></li>');*/
						}else if(userInfo.userMessageIdx != 1 && event.fromUserIdx ==userInfo.userMessageIdx ){
							//관리자가 승인해준 메시지
							$("#chat-messages").append('<li class="me"><p class="name">'+event.name+'</p><div class="cont">'
									+'<span class="bg_top"></span><p class="txt">'+event.msg+'</p><span class="bg_bottom"></span>'+
									'</div></li>');
							scrollBottom();
						}else if(userInfo.userMessageIdx != 1 && event.fromUserIdx!=userInfo.userMessageIdx &&  event.name == 'admin'){
							//관리자의 전체 말 //유저 화면에서 받는 쪽
							$('#chat-messages').append('<li class="admin"><div class="clear"><p class="name fl">'+chatNotice.fromText+'</p>'+
									'<div class="fr"></div></div><div class="cont"><span class="bg_top"></span><p class="txt">'+event.msg+'</p>'+
									'<span class="bg_bottom"></span></div></li>');
							scrollBottom();
						}else{
							//다른 사람의 승인 메시지
							$('#chat-messages').append('<li class="user"><p class="name"><div class="clear">'+event.name+''
									 +'<div class="fr">'+
										'</div></div></p><div class="cont"><span class="bg_top"></span><p class="txt">'+event.msg+'</p><span class="bg_bottom"></span>'+
										'</div></li>');
							scrollBottom();
						}

						//모바일
						if(userInfo.userMessageIdx == 1 && event.fromUserIdx==userInfo.userMessageIdx ){
							$('#mo-chat-messages').prepend('<li class="admin"><div class="clear"><p class="name fl">'+chatNotice.fromText+'</p>'+
									'<div class="fr">'+
									'</div></div><div class="cont"><span class="bg_top"></span><p class="txt">'+event.msg+'</p>'+
									'<span class="bg_bottom"></span></div></li>');

						}else if(userInfo.userMessageIdx == 1 && event.fromUserIdx!=userInfo.userMessageIdx && event.name != 'admin'){
							//관리자가 승인 후 배경이 회색에서 흰색이 되기 때문에 굳이 메시지를 한 번더 표현 할 필요 없음;
							/*$('#chat-messages').append('<li class="me"><p class="name"><div class="clear">'+event.name+'<a href="#"><img src="/img/admin/ico_caution.png" alt="" /></a>'
									 +'<div class="fr"><a href="#"><img src="/img/admin/ico_t2.png" alt="" /></a><a href="#"><img src="/img/admin/ico_t3.png" alt="" /></a>'+
										'</div></div></p><div class="cont"><span class="bg_top"></span><p class="txt">'+event.msg+'</p><span class="bg_bottom"></span>'+
										'</div></li>');*/
						}else if(userInfo.userMessageIdx != 1 && event.fromUserIdx ==userInfo.userMessageIdx ){
							$("#mo-chat-messages").prepend('<li class="me"><p class="name">'+event.name+'</p><div class="cont">'
									+'<span class="bg_top"></span><p class="txt">'+event.msg+'</p><span class="bg_bottom"></span>'+
									'</div></li>');
							scrollTop();
						}else if(userInfo.userMessageIdx != 1 && event.fromUserIdx!=userInfo.userMessageIdx &&  event.name == 'admin'){
							$('#mo-chat-messages').prepend('<li class="admin"><div class="clear"><p class="name fl">'+chatNotice.fromText+'</p>'+
									'<div class="fr"></div></div><div class="cont"><span class="bg_top"></span><p class="txt">'+event.msg+'</p>'+
									'<span class="bg_bottom"></span></div></li>');
							scrollTop();
						}else{
							$('#mo-chat-messages').prepend('<li class="user"><p class="name"><div class="clear">'+event.name+''
									 +'<div class="fr">'+
										'</div></div></p><div class="cont"><span class="bg_top"></span><p class="txt">'+event.msg+'</p><span class="bg_bottom"></span>'+
										'</div></li>');
							scrollTop();
						}
					}
					break;
				case EventType.REQ_APPROVAL_MSG:
					console.log('REQ_APPROVAL_MSG');
					//최종형태
					var req_approval_msg = $('<li>', {
						'class':'me'
					}).html('<p class="name"><div class="clear">'+event.name+'<a href="javascript:void(0)" onclick="addBlackList(this,'+event.fromUserIdx+',\''+event.userId+'\',\''+event.name+'\');" class="black'+event.fromUserIdx +'" id="black'+event.fromUserIdx +'"><img src="/img/admin/ico_caution.png" alt="" /></a>'
							+'<div class="fr"><a href="javascript:void(0)" onclick="sendApprove(this,' + event.idx+ ');"><img src="/img/admin/ico_t1.png"><a/><a href="javascript:void(0)" onclick="speakerSendMessage(this);"><img src="/img/admin/ico_t2.png" alt="" /></a><a href="javascript:void(0)" onclick="goDirectMessage('+event.from_userIdx+',\''+event.userId+'\')"><img src="/img/admin/ico_t4.png" alt="" /></a>'+
							'</div></div></p><div class="cont"><span class="bg_top"></span><p class="txt">'+event.msg+'</p><span class="bg_bottom"></span>'+
							'</div>');

					var approve_button = $('<button style="display:none;">', {
						'type':'button',
						//'class': 'btn btn-default col-lg-6',
						'id' : 'approve',

					}).html('Approve');
					req_approval_msg.append(approve_button);
/*					var reject_button = $('<button>', {
						'type':'button',
						//'class': 'btn btn-default col-lg-6',
						'id' : 'reject',
					}).html('Reject');*/
					//req_approval_msg.append(reject_button);

				/*	var spanBlock = $('<span>', {
						'class': 'col-lg-2 enableblock',
						'style': 'padding:0px;border:1px solid black;text-align:center;',
						'data-user': userIdx,
					}).html('B');

					spanBlock.click(function(e) {
						if (spanBlock.hasClass('enableblock')) {
							var useridx = $(e.target).attr('data-user');
							ChatClient.addBlackList(useridx, function() {
								spanBlock.removeClass('enableblock').addClass('disableblock');
							});
						} else {
							var useridx = $(e.target).attr('data-user');
							ChatClient.removeBlackList(useridx, function() {
								spanBlock.removeClass('disableblock').addClass('enableblock');
							});
						}
					});*/
					approve_button.click(function() {
						ChatClient.approveMessage(event.fromUserIdx, event.userId, event.name, event.msg);
						approve_button.parent("li").removeClass("me");
						approve_button.parent("li").addClass("approve");
						approve_button.remove();
						//reject_button.remove();
					});
					/*reject_button.click(function() {
						ChatClient.rejectMessage(event.fromUserIdx, event.userId, event.name, event.msg);
						$(this).parent("li").remove();
					});*/
					$('#chat-messages').append(req_approval_msg);
					$('#mo-chat-messages').prepend(req_approval_msg);

					scrollBottom();
					break;
				case EventType.WAIT_APPROVAL_MSG:
					console.log('WAIT_APPROVAL_MSG');
					$('#chat-messages').append('<li class="approval"><div class="clear"><p class="name">'+event.name+'</p>'+
							'<div class="fr">'+
							'</div></div><div class="cont"><span class="bg_top"></span><p class="txt">'+
							''+chatNotice.noticeTextAdmin+'<br/>'+chatNotice.noticeTextMe+''+event.msg+'</p>'+
							'<span class="bg_bottom"></span></div></li>');
					$('#mo-chat-messages').prepend('<li class="approval"><div class="clear"><p class="name">'+event.name+'</p>'+
							'<div class="fr">'+
							'</div></div><div class="cont"><span class="bg_top"></span><p class="txt">'+
							''+chatNotice.noticeTextAdmin+'<br/>'+chatNotice.noticeTextMe+''+event.msg+'</p>'+
							'<span class="bg_bottom"></span></div></li>'); //'관리자 승인 대기중 입니다. <br/>'+chatNotice.noticeTextMe+' : '+event.msg+'</p>'+

					scrollBottom();
					scrollTop();

					break;
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				case EventType.APPROVED_MSG:
					//$('#chat-messages').append('<li class="list-group-item chat-message col-lg-12">msg Approved</li>');
					break;
				case EventType.REJECTED_MSG:
					//$('#chat-messages').append('<li class="list-group-item chat-message col-lg-12">msg rejected</li>');
					break;
				case EventType.DIRECT_MSG:
					console.log('DIRECT_MSG');
					//alert(userInfo.userName);
					var value = userInfo.toUserId.split("_");
					//관리자가 보내고 관리자 화면에서 보기 위한 뷰 제어
					if(userInfo.userMessageIdx == 1 && event.fromUserIdx==userInfo.userMessageIdx){
						$('#chat-messages').append('<li class="admin"><div class="clear"><p class="name fl">'+chatNotice.fromText+'</p>'+
								'<div class="fr"></div></div><div class="cont"><span class="bg_top"></span><p class="txt">'+
								'('+value[0]+')'+value[1]+' 에게 전달</p><p class="txt">'+
								event.msg+'</p>'+'<span class="bg_bottom"></span></div></li>');
					}else{
						$('#chat-messages').append('<li class="admin"><div class="clear"><p class="name fl">'+chatNotice.fromText+'</p>'+
								'<div class="fr"></div></div><div class="cont"><span class="bg_top"></span><p class="txt">'+
								event.msg+'</p>'+'<span class="bg_bottom"></span></div></li>');
						$('#mo-chat-messages').prepend('<li class="admin"><div class="clear"><p class="name fl">'+chatNotice.fromText+'</p>'+
								'<div class="fr"></div></div><div class="cont"><span class="bg_top"></span><p class="txt">'+event.msg+'</p>'+
								'<span class="bg_bottom"></span></div></li>');
					}
					scrollBottom();
					break;
				case EventType.ADMIN_MSG:
					console.log('ADMIN_MSG')
					$('#chat-messages').append('<li class="list-group-item chat-message admin_message col-lg-12">' + event.userId + '(' + event.name + ') : ' + event.msg + '</li>');
					break;
				case EventType.BLOCKED_MSG:
					console.log('BLOCKED_MSG');
					//$('#chat-messages').append('<li class="list-group-item chat-message blocked_message col-lg-12">Your chat is blocked.</li>');
					$('#chat-messages').append('<li class="admin"><div class="clear"><p class="name fl">'+chatNotice.fromText+'</p>'+
							'<div class="fr">'+
							'</div></div><div class="cont"><span class="bg_top"></span><p class="txt">'+
							'관리자에 의해 메시지전송 거부 상태입니다.</p>'+
							'<span class="bg_bottom"></span></div></li>');
					$('#mo-chat-messages').prepend('<li class="admin"><div class="clear"><p class="name fl">'+chatNotice.fromText+'</p>'+
							'<div class="fr">'+
							'</div></div><div class="cont"><span class="bg_top"></span><p class="txt">'+
							'관리자에 의해 메시지전송 거부 상태입니다.</p>'+
							'<span class="bg_bottom"></span></div></li>');
					scrollBottom();
					scrollTop();
					break;
				case EventType.CREATE_CHATROOM:
					var tr = $('<tr>', {
						id: "ROOM_" + event.programIdx
					}).append('<td>' + event.name + '</td><td>' + event.userId + '</td><td>' + event.msg +'</td>');
					tr.click(function() {
						ChatClient.enterChatRoom(room.programIdx, '', '', '', 0, function(data) {
							enterChatRoom(event.programIdx, data.name, data.userIdx);
						});
					});
					$('#chat-room').append(tr);
					console.log('CREATE_CHATROOM')
					break;
				case EventType.REMOVE_CHATROOM:
					$('#ROOM_'+ event.programIdx).remove();
					console.log('REMOVE_CHATROOM')
					break;
				case EventType.ENTER_USER:
					addUserToUserList(event.fromUserIdx, event.userId, event.name);
					console.log('ENTER_USER')
					break;
				case EventType.LEAVE_USER:
					$('#USER_'+ event.fromUserIdx).remove();
					console.log('LEAVE_USER')
					break;
				}
			});
		}//if
	};



	var create_enter_room = function(name,description,type,programIdx, adminIdx) {
	    console.log( "Create Room Logging" );
		if (name && name.length !== 0 ) {
            ChatClient.enterChatRoom(programIdx, adminIdx, name, description, type,
               function(data) { enterChatRoom(programIdx, data.name, data.userIdx);
			});
		} else {
			//alert('Please input chat room name!!!');
		}
	};

	var goDirectMessage = function(userIdx,userId,msg) {
		console.log("goDirectMessage 들어옴");
		userInfo.toUserId = userId;
		ChatClient.setUserId(userInfo.toUserId);

		if (msg && msg.length !== 0) {
			ChatClient.sendDirectMessage(userIdx, msg, function() {
				$('#chat-message').val('');
			});
		}
	};

	var init = function () {
		var userId = $('#userinfo-userId').val();

		var noticeTextAdmin = "관리자에게 전달 되었습니다.";
		var noticeTextMe  = "나의 메시지 : ";
		var fromText = "운영담당자";

		if($('.chat-languages').length == 1){
			if($('.chat-languages').val() =='ENGLISH'){
				noticeTextAdmin = "Sent to manager."
				noticeTextMe = "My message : "
				fromText = "Manager";
			}
		}

		chatNotice.noticeTextAdmin = noticeTextAdmin;
		chatNotice.noticeTextMe = noticeTextMe;
		chatNotice.fromText = fromText;


		if(userId!=='admin'){

        	//alert("유저 접속");
        	var name = $('#create-chatroom-name').val(); //programIdx 번호 입니다.
        	if($('#create-chatroom-name').val()===""){
        		name="1";
        	}
        	var description = "Description" ; //Chat Room Description
        	var type = Number($("#create-chatroom-type").val());//0:MANYTOMANY, 1:ONETOMANY, 2:APPROVAL


        	var programIdx = name;
        	var userId = $('#userinfo-userid').val();
        	if(!userId || userId.length === 0){
        		userId = "userId"+Math.floor((Math.random() * 1000) + 1);
        		//userId = "1_2";
        	}
			var userIdx = $('#userinfo-useridx').val();
			if(!userIdx){
				userIdx = Math.floor((Math.random() * 1000) + 1);
			}
			var userName = $('#userinfo-username').val();
			if(!userName){
				if(userId){
					userName = userId;
				}else{
					userName= Math.floor((Math.random() * 1000) + 1);
				}
			}

			userInfo.userMessageIdx = userIdx;
			userInfo.chatRoomType = type;

			ChatClient.setUserInfo(userIdx, userId, userName, false);

	        //ChatClient.enterChatRoom(programIdx, ChatClient.getUserIdx(), name, description, type, function(data) {
			 ChatClient.enterChatRoom(programIdx, 1 , name, description, Number(type), function(data) {
				 ChatClient.getNewEvent(processEvents);
				 //alert(programIdx+","+data.programIdx+","+data.userIdx);
			     enterChatRoom(programIdx, data.name, data.userIdx);
			});


			if(type===0){
				ChatClient.getBeforeMessage(userId,userIdx,name,type,function(data){
					 for(var i=0; i<data.length; i++){
						 //관리자
						 if((data[i].from_userIdx ==1 && data[i].type==0) ||
								 (data[i].from_userIdx ==1 && data[i].type==1 && userInfo.userMessageIdx == data[i].to_userIdx )){
							 $('#chat-messages').append('<li class="admin"><div class="clear"><p class="name fl">'+chatNotice.fromText+'</p>'+
										'<div class="fr">'+
										'</div></div><div class="cont"><span class="bg_top"></span><p class="txt">'+data[i].message+'</p>'+
										'<span class="bg_bottom"></span></div></li>');
						 }else if(userInfo.userMessageIdx == data[i].from_userIdx){
							 //내꺼
							 $('#chat-messages').append('<li class="me"><p class="name">'+data[i].name+'</p><div class="cont">'
										+'<span class="bg_top"></span><p class="txt">'+data[i].message+'</p><span class="bg_bottom"></span>'+
										'</div></li>');
						 }else if(data[i].from_userIdx !=1){
							 //다른 사람
							 $('#chat-messages').append('<li class="user"><p class="name"><div class="clear">'+data[i].name+''
									 +'<div class="fr">'+
										'</div></div></p><div class="cont"><span class="bg_top"></span><p class="txt">'+data[i].message+'</p><span class="bg_bottom"></span>'+
										'</div></li>');
						 }
						 if((data[i].from_userIdx ==1 && data[i].type==0) ||
								 (data[i].from_userIdx ==1 && data[i].type==1 && userInfo.userMessageIdx == data[i].to_userIdx )){
							 $('#mo-chat-messages').prepend('<li class="admin"><div class="clear"><p class="name fl">'+chatNotice.fromText+'</p>'+
										'<div class="fr">'+
										'</div></div><div class="cont"><span class="bg_top"></span><p class="txt">'+data[i].message+'</p>'+
										'<span class="bg_bottom"></span></div></li>');
						 }else if(userInfo.userMessageIdx == data[i].from_userIdx){
							 $('#mo-chat-messages').prepend('<li class="me"><p class="name">'+data[i].name+'</p><div class="cont">'
										+'<span class="bg_top"></span><p class="txt">'+data[i].message+'</p><span class="bg_bottom"></span>'+
										'</div></li>');
						 }else if(data[i].from_userIdx !=1){
							 $('#mo-chat-messages').prepend('<li class="user"><p class="name"><div class="clear">'+data[i].name+''
									 +'<div class="fr">'+
										'</div></div></p><div class="cont"><span class="bg_top"></span><p class="txt">'+data[i].message+'</p><span class="bg_bottom"></span>'+
										'</div></li>');
						 }
					 }
					 scrollTop();
					 scrollBottom();
				 });
			}else if(type===1){
				 ChatClient.getBeforeMessage(userId,userIdx,name,type,function(data){
					 for(var i=0; i<data.length; i++){
						 //웹
						 if(data[i].name=="admin"){
							 $('#chat-messages').append('<li class="admin"><div class="clear"><p class="name fl">'+"Admin"/*data[i].name*/+'</p>'+
										'<div class="fr"></div></div><div class="cont"><span class="bg_top"></span><p class="txt">'+data[i].message+'</p>'+
										'<span class="bg_bottom"></span></div></li>');
						 }else{
							 $('#chat-messages').append('<li class="me"><p class="name">'+data[i].name+'</p><div class="cont">'
										+'<span class="bg_top"></span><p class="txt">'+data[i].message+'</p><span class="bg_bottom"></span>'+
										'</div></li>');
						 }
						 //모바일
						 if(data[i].name=="admin"){
							 $('#mo-chat-messages').append('<li class="admin"><div class="clear"><p class="name fl">'+"Admin"/*data[i].name*/+'</p>'+
										'<div class="fr"></div></div><div class="cont"><span class="bg_top"></span><p class="txt">'+data[i].message+'</p>'+
										'<span class="bg_bottom"></span></div></li>');
						 }else{
							 $('#mo-chat-messages').append('<li class="me"><p class="name">'+data[i].name+'</p><div class="cont">'
										+'<span class="bg_top"></span><p class="txt">'+data[i].message+'</p><span class="bg_bottom"></span>'+
										'</div></li>');
						 }
					 }
				 });
			}else{//type2
				 ChatClient.getBeforeMessage(userId,userIdx,name,type,function(data){
					 for(var i=0; i<data.length; i++){
						 //웹
						 //관리자가 보낸 메시지
						 if((data[i].type === 0 && data[i].name === "admin")|| (data[i].from_userIdx ==1 && data[i].type==1 && userInfo.userMessageIdx == data[i].to_userIdx)){
							 $('#chat-messages').append('<li class="admin"><div class="clear"><p class="name fl">'+chatNotice.fromText+'</p>'+
										'<div class="fr"></div></div><div class="cont"><span class="bg_top"></span><p class="txt">'+data[i].message+'</p>'+
										'<span class="bg_bottom"></span></div></li>');
						 }else if((data[i].type === 3 && data[i].name !=="admin") && userInfo.userMessageIdx == data[i].from_userIdx){
							//내가보낸 승인 전 메시지
							$('#chat-messages').append('<li class="approval"><div class="clear"><p class="name">'+data[i].name+'</p>'+
										'<div class="fr">'+
										'</div></div><div class="cont"><span class="bg_top"></span><p class="txt">'+
										''+chatNotice.noticeTextAdmin+' <br/>'+chatNotice.noticeTextMe+''+data[i].msg+'</p>'+
										'<span class="bg_bottom"></span></div></li>');
						 }else if(data[i].type === 30 && userInfo.userMessageIdx == data[i].from_userIdx){
							//내가보낸 승인 된 메시지
							 $('#chat-messages').append('<li class="me"><p class="name">'+data[i].name+'</p><div class="cont">'
										+'<span class="bg_top"></span><p class="txt">'+data[i].message+'</p><span class="bg_bottom"></span>'+
										'</div></li>');
						 }else if(data[i].type === 30 && userInfo.userMessageIdx != data[i].from_userIdx){
							 //다른 사람이 보낸 승인 된 메시지
							 $('#chat-messages').append('<li class="user"><p class="name"><div class="clear">'+data[i].name+''
									 +'<div class="fr">'+
										'</div></div></p><div class="cont"><span class="bg_top"></span><p class="txt">'+data[i].message+'</p><span class="bg_bottom"></span>'+
										'</div></li>');
						 }
						 //모바일
						 if((data[i].type === 0 && data[i].name === "admin")|| (data[i].from_userIdx ==1 && data[i].type==1 && userInfo.userMessageIdx == data[i].to_userIdx)){
							 $('#mo-chat-messages').prepend('<li class="admin"><div class="clear"><p class="name fl">'+chatNotice.fromText+'</p>'+
										'<div class="fr"></div></div><div class="cont"><span class="bg_top"></span><p class="txt">'+data[i].message+'</p>'+
										'<span class="bg_bottom"></span></div></li>');
						 }else if((data[i].type === 3 && data[i].name !=="admin") && userInfo.userMessageIdx == data[i].from_userIdx){
							 $('#mo-chat-messages').prepend('<li class="approval"><div class="clear"><p class="name">'+data[i].name+'</p>'+
										'<div class="fr">'+
										'</div></div><div class="cont"><span class="bg_top"></span><p class="txt">'+
										''+chatNotice.noticeTextAdmin+' <br/>'+chatNotice.noticeTextMe+''+data[i].msg+'</p>'+
										'<span class="bg_bottom"></span></div></li>');
							 //에러 날시 타입 5로 변경
						 }else if(data[i].type === 30 && userInfo.userMessageIdx == data[i].from_userIdx){
							 $('#mo-chat-messages').prepend('<li class="me"><p class="name">'+data[i].name+'</p><div class="cont">'
										+'<span class="bg_top"></span><p class="txt">'+data[i].message+'</p><span class="bg_bottom"></span>'+
										'</div></li>');
							 //에러 날시 타입 5로 변경
						 }else if(data[i].type === 30 && userInfo.userMessageIdx != data[i].from_userIdx){
							 $('#mo-chat-messages').prepend('<li class="user"><p class="name"><div class="clear">'+data[i].name+''
									 +'<div class="fr">'+
										'</div></div></p><div class="cont"><span class="bg_top"></span><p class="txt">'+data[i].message+'</p><span class="bg_bottom"></span>'+
										'</div></li>');
						 }
					 }//for
					 scrollTop();
					 scrollBottom();
				 });

			}//else
        }else{
        	var name = $('#create-chatroom-name').val(); //programIdx 번호 입니다.
    		var description = "Description" ; //Chat Room Description
    		var type = Number($("#create-chatroom-type").val());//0:MANYTOMANY, 1:ONETOMANY, 2:APPROVAL
    		//var type = $('#create-chatroom-type').val(); //0:MANYTOMANY, 1:ONETOMANY, 2:APPROVAL
    		var programIdx = name;
    		var userId = $('#userinfo-userId').val();
    		var userIdx = $('#userinfo-userIdx').val();
    		var userName = $('#userinfo-userName').val();//Math.floor((Math.random() * 1000) + 1);
    		var programName = $('#programName').val();
    		userInfo.userMessageIdx = userIdx;
    		userInfo.chatRoomType = type;

			if (userId && userId.length !== 0) {
				ChatClient.setUserInfo(userIdx, userId, userName, true);
				ChatClient.enterChatRoom(programIdx, userIdx, name, description, type, function(data) {
					 ChatClient.getNewEvent(processEvents);
				     enterChatRoom(programIdx, data.name, data.userIdx);
				 });

				 if(type===0){
					 ChatClient.getBeforeMessage(userId,userIdx,name,type,function(data){
						 for(var i=0; i<data.length; i++){
							 if(data[i].name=="admin"){
								 var value = '';
								 var text ='';
								 if(data[i].to_UserId !='' && data[i].to_UserId != null){
									 value = data[i].to_UserId.split("_");
									 text = '<p class="txt">'+'('+value[0]+')'+value[1]+' 에게 전달</p>';
								 }
								 $('#chat-messages').append('<li class="admin"><div class="clear"><p class="name fl">'+chatNotice.fromText+'</p>'+
											'<div class="fr">'+
											'</div></div><div class="cont"><span class="bg_top"></span>'+text+'<p class="txt">'+data[i].message+'</p>'+
											'<span class="bg_bottom"></span></div></li>');
							 }else{
								 $('#chat-messages').append('<li class="approve"><p class="name"><div class="clear">'+data[i].name+'<a href="javascript:void(0)" class="black'+data[i].fromUserIdx+'" onclick="addBlackList(this,'+data[i].from_userIdx+',\''+data[i].userId+'\',\''+data[i].name+'\')"><img src="/img/admin/ico_caution.png" alt="" /></a>'
										 +'<div class="fr"><a href="javascript:void(0)" onclick="speakerSendMessage(this);"><img src="/img/admin/ico_t2.png" alt="" /></a><a href="javascript:void(0)" onclick="goDirectMessage('+data[i].from_userIdx+',\''+data[i].userId+'\')"><img src="/img/admin/ico_t4.png" alt="" /></a>'+
											'</div></div></p><div class="cont"><span class="bg_top"></span><p class="txt">'+data[i].message+'</p><span class="bg_bottom"></span>'+
											'</div></li>');
							 }
						 }
						 scrollBottom();
					 });
				 }else if(type===1){
					 ChatClient.getBeforeMessage(userId,userIdx,name,type,function(data){
						 for(var i=0; i<data.length; i++){
							 if(data[i].name=="admin"){
								 $('#chat-messages').append('<li class="admin"><div class="clear"><p class="name fl">'+"Admin"/*data[i].name*/+'</p>'+
											'<div class="fr">'+
											'</div></div><div class="cont"><span class="bg_top"></span><p class="txt">'+data[i].message+'</p>'+
											'<span class="bg_bottom"></span></div></li>');
							 }else{
								 $('#chat-messages').append('<li class="me"><p class="name"><div class="clear">'+data[i].name+'<a href="#"><img src="/img/admin/ico_caution.png" alt="" /></a>'
										 +'<div class="fr"><a href="#"><img src="/img/admin/ico_t2.png" alt="" /></a><a href="#"><img src="/img/admin/ico_t3.png" alt="" /></a>'+
											'</div></div></p><div class="cont"><span class="bg_top"></span><p class="txt">'+data[i].message+'</p><span class="bg_bottom"></span>'+
											'</div></li>');
							 }
						 }
					 });
//				 type 2
				 }else{
					 ChatClient.getBeforeMessage(userId,userIdx,name,type,function(data){
						 for(var i=0; i<data.length; i++){
							 //
							 if(data[i].name == "admin"){
								 var value = '';
								 var text ='';
								 if(data[i].to_UserId !='' && data[i].to_UserId != null){
									 value = data[i].to_UserId.split("_");
									 text = '<p class="txt">'+'('+value[0]+')'+value[1]+' 에게 전달</p>';
								 }
								 $('#chat-messages').append('<li class="admin"><div class="clear"><p class="name fl">'+chatNotice.fromText+'</p>'+
											'<div class="fr">'+
											'</div></div><div class="cont"><span class="bg_top"></span>'+text+'<p class="txt">'+data[i].message+'</p>'+
											'<span class="bg_bottom"></span></div></li>');
							 }else{ //type3 = 승인 대기 메시지 type 5 = 승인 메시지
								 if(data[i].type==30){
								 		$('#chat-messages').append('<li class="approve"><p class="name"><div class="clear">'+data[i].name+'<a href="javascript:void(0)" class="black'+data[i].fromUserIdx+'" onclick="addBlackList(this,'+data[i].from_userIdx+',\''+data[i].userId+'\',\''+data[i].name+'\')"><img src="/img/admin/ico_caution.png" alt="" /></a>'
										 +'<div class="fr"><a href="javascript:void(0)" onclick="speakerSendMessage(this);"><img src="/img/admin/ico_t2.png" alt="" /></a><a href="javascript:void(0)" onclick="goDirectMessage('+data[i].from_userIdx+',\''+data[i].userId+'\')"><img src="/img/admin/ico_t4.png" alt="" /></a>'+
											'</div></div></p><div class="cont"><span class="bg_top"></span><p class="txt">'+data[i].msg+'</p><span class="bg_bottom"></span>'+
											'</div></li>');
								 	}else if(data[i].type == 3 && data[i].name != "admin"){
								 		var req_approval_msg = $('<li>', {
											'class':'me'
										}).html('<p class="name"><div class="clear">'+data[i].name+'<a href="javascript:void(0)" class="black'+data[i].fromUserIdx+'" id="black'+data[i].from_userIdx+'" onclick="addBlackList(this,'+data[i].from_userIdx+',\''+data[i].userId+'\',\''+data[i].name+'\')"><img src="/img/admin/ico_caution.png" alt=""/></a>'
												 +'<div class="fr"><a href="javascript:void(0)" onclick="beforesendApprove(this,'+data[i].idx+','+data[i].from_userIdx+',\''+data[i].userId+'\',\''+data[i].name+'\',\''+data[i].msg+'\');">'
												 +'<img src="/img/admin/ico_t1.png"></a><a href="#" onclick="speakerSendMessage(this);"><img src="/img/admin/ico_t2.png" alt="" /></a><a href="javascript:void(0)" onclick="goDirectMessage('+data[i].from_userIdx+',\''+data[i].userId+'\')"><img src="/img/admin/ico_t4.png" alt="" /></a>'+
													'</div></div></p><div class="cont"><span class="bg_top"></span><p class="txt">'+data[i].msg+'</p><span class="bg_bottom"></span>'+
													'</div></li>');

										var approve_button = $('<button style="display:none;">', {
											'type':'button',
											//'class': 'btn btn-default col-lg-6',
											'id' : 'approve',
										}).html('Approve');
										req_approval_msg.append(approve_button);
										$('#chat-messages').append(req_approval_msg);
								 	}//else if
							 }//else
						 }//for
						 scrollBottom();
					 });
				 }//else

				 ChatClient.getBlackUserList(name,function(data){
					 console.log(data);
					 for(var i=0; i<data.length; i++){//<span>"+data[i].userId.substring(0,(data[i].userId.length)-(data[i].name.length+1))+"</span>
						 $("#scroll_area").append("<li><span style='width:66%;'>"+data[i].name+"</span><span><a href='javascript:void(0)' class='remove"+data[i].to_userIdx+"'  onclick='removeBlackList("+"this,"+data[i].to_userIdx+")'>해지</a></span></li>");

						 $(".black"+data[i].to_userIdx).each(function(index){
								$(this).css("display","none");
						 });
					 }
				 });
			}//if userId && userId.length !== 0
        }//else


		$('#chatroom-exit-btn').click(function() {
			exitChatRoom(true);
		});

		$("a[id='send-message-btn']").click(function() {
			var send_message_btn = $(this).parent().find(":input");
			var msg = send_message_btn.val();
			var result = true;
			var checkChar = new Array("<script>","</script>");

			for(var i=0; i<checkChar.length; i++){
				var iValue = msg.indexOf(checkChar[i]);
				if(iValue != -1){
					alert("사용할 수 없는 문자열이 있습니다."); // 해당 문자열이 없으면 -1 을 리턴 한다.
					result = false;
					send_message_btn.val('');
					return false;
				}
			}
			for(var i=0; i<msg.length; i++){
				msg = msg.replace("<", "&lt;");
				msg = msg.replace(">", "&gt;");
			};
			if(msg.length > 340){
				alert("글자 수 입력제한! 340자 까지만 입력하실 수 있습니다.");
				send_message_btn.val('');
				result = false;
				return false;
			}

			//귓속말 기능으로 추가 된 소스 입니다.
			//userId 를 검사해서 undefined 면 유저 창에서 전송을 한것입니다. (이뉴 : 유저 화면에는 selectbox 가 정의되지 않았음)
			//그렇지 않으면  idx 값이 넘어 오게 됩니다.  2번째 if 문에서 admin 이 아닐때를 검사해서 귓속말과 전체 채팅을 구분합니다.
			//여기는 마우스로 버튼을 클릭했을 때 이벤트 영역이며, 엔터는 program_live  에 있습니다.
			var userId = $('#selectUser').val();

			//유저가 채팅을 입력했을 때는 select box 가 유저 화면에는 없기 때문에 userId 가 undefined 로 정의된다.
			if(typeof userId == 'undefined'){
				if(result){
					if (msg && msg.length !== 0) {
						ChatClient.sendMessage(msg, function() {
							send_message_btn.val('');
						});
					}
				}
			}else if(userId =='admin'){
				//관리자가 전체선택 상태로 보낸것을 userId 값이 admin 이다.
				if(result){
					if (msg && msg.length !== 0) {
						ChatClient.sendMessage(msg, function() {
							send_message_btn.val('');
						});
					}
				}
			}else if(userId !='admin'){
				//관리자가 마우스로 버튼을 눌렀을 때  select box 선택값이 admin 아니기 때문에 귓속 기능을 수행 해줘야한다.
				$("#send-direct-message-btn").click();
     			return;
			}
		});
//		문법
/*		$("a[id='send-message-btn']").click(function() {
			var ctrl = $(this).parent().find(":input");
			var msg = ctrl.val();
			alert(msg);
			//return;
			if (msg && msg.length !== 0) {
				ChatClient.sendMessage(msg, function() {
					ctrl.val('');
				});
			}
		});
*/
		$('#send-admin-message-btn').click(function() {
			var msg = $('#admin_message').val();
			if (msg && msg.length !== 0) {
				ChatClient.sendAdminMessage(msg, function() {
					$('#admin_message').val('');
				});
			}
		});
		$('#send-direct-message-btn').click(function() {
			var userIdx = $('#selectUser').val();
			//관리자 창에서 귓속말 누구한테 해준것인지 체크하기 위한 변수 선언
			var toUserId = $('#selectUser option:selected').text();

			userInfo.toUserId = toUserId;
			ChatClient.setUserId(userInfo.toUserId);

			var msg = $('#chat-message').val();

			if (msg && msg.length !== 0) {
				ChatClient.sendDirectMessage(userIdx, msg, function() {
					$('#chat-message').val('');
				});
			}
		});
	};

	return {
		init: init,
		exitChatRoom : exitChatRoom,
		goDirectMessage : goDirectMessage
	};
}) ();
