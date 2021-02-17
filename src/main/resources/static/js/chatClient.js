var ChatClient = function() {
	var userInfo = {
		internalIdx : -1,
		programIdx: -1,
		userIdx : -1,
		userId : '',
		userName : '',
		isAdmin : false
	};

	var setUserInfo = function(userIdx, userId, userName, isAdmin) {
		userInfo.userIdx = userIdx;
		userInfo.userId = userId;
		userInfo.userName = userName;
		userInfo.isAdmin = isAdmin;
	}

	var toUserId = '';
	
	var getUserInfo = function() {
		return userInfo;		
	};
	
	var getUserIdx = function() {
		return userInfo.userIdx;
	};
	

	var setUserId = function(userId){
		toUserId = userId;
	}

	var enterChatRoom = function(programIdx, adminIdx, name, description, type, callback) {
		if (!userInfo || userInfo.programIdx === -1) {
			var sendData = {
				programIdx : programIdx,
				adminIdx: adminIdx,
				name: name,
				description: description,
				type: type,
			};

			$.ajax({
				method : "POST",
				url : '/chattingRoom/enterUser',
				contentType : 'application/json; charset=UTF-8',
				headers : userInfo,
				data : JSON.stringify(sendData)
			}).done(function(data){
				userInfo.internalIdx = data.internalIdx;
				userInfo.programIdx = data.programIdx;
				if (callback) {
					callback(data);
				}
			}).fail(function(data) {
				var keys = Object.keys(data);
				var obj = JSON.parse(data[keys[18]]);
				if(obj.exception==="server.chat.exceptions.UserExistException" && obj.error==="Bad Request"){
					alert("참여중인 방송이 있습니다.");
					location.href ="/chat/overlapUser";
				}else{
					callback(data);
				}
			});
		}
	};
	
	var exitChatRoom = function(async, callback) {
		if (typeof async === 'undefined') {
			async = false;
		}
		if (userInfo.programIdx !== -1) {
			$.ajax({
				method: "DELETE",
				url: '/chatRoom/user',
				contentType:'application/json; charset=UTF-8',
				async: async,
				headers: userInfo
			}).done(function(data){
				userInfo.programIdx = -1;
				if (callback) {
					callback(data);
				}
			});				
		}
	};
	
	var updateChatRoom = function (updateInfo, callback) {
		updateInfo.programIdx = userInfo.programIdx;
		
		if (!updateInfo.type) {
			updateInfo.type = -1;
		}
		if (!updateInfo.adminIdx) {
			updateInfo.adminIdx = -1;
		}
		
		$.ajax({
			method: "PUT",
			url: '/chatRoom',
			contentType:'application/json; charset=UTF-8',
			headers: userInfo,
			data: JSON.stringify(updateInfo)
		}).done(function(data){
			if (callback) {
				callback(data);
			}
		});
	}

	var getUserList = function(callback) {
		$.ajax({
			method: "GET",
			url: '/chattingRoom/users',
			contentType:'application/json; charset=UTF-8',
			cache: false, //새로 추가 16.10.04  IE에서 기존 유저 새로고침 할 시 나감 처리 및 새로운 유저 입장 처리 해주기 위해 캐쉬 false;
			data: {
				'programIdx' : userInfo.programIdx
			},
		}).done(function(data){
			if (callback) {
				callback(data);
			}
		});
	};
	
	var getChatRoomList = function(callback) {
		//alert("getChatRoomList");
		$.ajax({
			method: "GET",
			url: '/chatRoom/list',
			contentType:'application/json; charset=UTF-8',
			headers: userInfo,
		}).done(function(data){
			//alert("getChatRoomList done" + userInfo.programIdx);
			if (callback) {
				callback(data);
			}
		});
	};
	
	var getNewEvent = function(callback) {
		console.log('getNewEvent : start call');
		if (userInfo.userIdx !== -1 && userInfo.programIdx !== -1) {
			$.ajax({
				method: "GET",
				url: '/chattingRoom/event',
				contentType:'application/json; charset=UTF-8',
				cache: false,
				headers: userInfo
			}).done(function(data){
				if (callback) {
					callback(data);
				}
				if (userInfo && typeof userInfo.userIdx !== 'undefined' && userInfo.userIdx !== -1 && userInfo.programIdx !== -1) {
					getNewEvent(callback);
				}
			}).fail(function() {
				// when server is down
				setTimeout(function() {
					getNewEvent(callback);
				}, 1000);
			});
		};
	};
	
	var sendMessage = function(message, callback) {
		var sendData = {
			programIdx : userInfo.programIdx
			, fromUserIdx : userInfo.userIdx
			, userId : userInfo.userId
			, name : userInfo.userName
			, message : message
			, type : eventType.NORMAL_MSG
		};
		$.ajax({
			method: "POST",
			url: '/chattingRoom/event',
			contentType:'application/json; charset=UTF-8',
			headers: userInfo,
			data: JSON.stringify(sendData)
		}).done(function(data){
			console.log('sendMessage');
			if (callback) {
				callback(data);
			}
		}).fail(function(data){
			console.log(data);
			var keys = Object.keys(data);
			var obj = JSON.parse(data[keys[18]]);
			if(obj.exception==="server.chat.exceptions.BadArgumentException"){
				alert("참여중인 방송이 있습니다.");
				location.href ="/chat/overlapUser";
			}
		});			
	};
	
	var sendAdminMessage = function(msg, callback) {
		var sendData = {
				programIdx: userInfo.programIdx,
				fromUserIdx: userInfo.userIdx,
				userId: userInfo.userId,
				name: userInfo.userName,
				msg: msg,
				type: eventType.ADMIN_MSG
			};
		
			$.ajax({
				method: "POST",
				url: '/event',
				contentType:'application/json; charset=UTF-8',
				headers: userInfo,
				data: JSON.stringify(sendData)
			}).done(function(data){
				if (callback) {
					console.log("sendAdminMessage");
					callback(data);
				}
			});		
	};
	
	var sendDirectMessage = function(toUserIdx, msg, callback) {
		
		var sendData = {
			programIdx: userInfo.programIdx,
			fromUserIdx: userInfo.userIdx,
			userId: userInfo.userId,
			name: userInfo.userName,
			toUserIdx : toUserIdx,
			to_UserId : toUserId,
			msg: msg,
			type: eventType.DIRECT_MSG
		};
		$.ajax({
			method: "POST",
			url: '/event',
			contentType:'application/json; charset=UTF-8',
			headers: userInfo,
			data: JSON.stringify(sendData)
		}).done(function(data){
			if (callback) {
				console.log("sendDirectMessage");
				callback(data);
			}
		});		
	};
	
	var addBlackList = function(blackUser, callback) {
		var sendData = {
			programIdx: userInfo.programIdx,
			userIdx : blackUser,
		};
	
		$.ajax({
			method: "POST",
			url: '/chatRoom/blacklist',
			contentType:'application/json; charset=UTF-8',
			headers: userInfo,
			data: JSON.stringify(sendData)
		}).done(function(data){
			if (callback) {
				callback(data);
			}
		});			
	};
	
	var removeBlackList = function(blackUser, callback) {
		var sendData = {
			programIdx: userInfo.programIdx,
			userIdx : blackUser,
		};
		
		$.ajax({
			method: "DELETE",
			url: '/chatRoom/blacklist',
			contentType:'application/json; charset=UTF-8',
			headers: userInfo,
			data: JSON.stringify(sendData)
		}).done(function(data){
			if (callback) {
				callback(data);
			}
		});			
	};
	//[2016.10.17 한희성]블랙리스트를 받아오기 위해 function 추가
	var getBlackList = function(blackUser, callback) {
		$.ajax({
			method: "GET",
			url: '/chatRoom/blacklist',
			contentType:'application/json; charset=UTF-8',
			headers: userInfo,
		}).done(function(data){
			if (callback) {
				callback(data);
			}
		});			
	};
	
	var approveMessage = function(orgUserIdx, orgUserId, orgUserName, msg, callback) {
		var sendData = {
			programIdx: userInfo.programIdx,
			fromUserIdx: orgUserIdx,
			userId: orgUserId,
			name: orgUserName,
			msg: msg,
			type: eventType.APPROVED_MSG
		};
		$.ajax({
			method: "POST",
			url: '/event',
			contentType:'application/json; charset=UTF-8',
			headers: userInfo,
			data: JSON.stringify(sendData)
		}).done(function(data){
			if (callback) {
				console.log("approveMessage");
				callback(data);
			}
		});			
	};
	
	var rejectMessage = function(orgUserIdx, orgUserId, orgUserName, msg, callback) {
		var sendData = {
			programIdx: userInfo.programIdx,
			fromUserIdx: orgUserIdx,
			userId: orgUserId,
			name: orgUserName,
			msg: msg,
			type: eventType.REJECTED_MSG
		};
		
		$.ajax({
			method: "POST",
			url: '/event',
			contentType:'application/json; charset=UTF-8',
			headers: userInfo,
			data: JSON.stringify(sendData)
		}).done(function(data){
			if (callback) {
				callback(data);
			}
		});			
	};
	var getBeforeMessage = function(userId,userIdx,roomName,type,callback){
		$.ajax({
			method : "GET",
			url : "/message",
			contentType : "applcation/json; charset=UTF-8",
			data: {
				userId : userId,
				userIdx : userIdx,
				roomName : roomName,
				type : type
			}
		}).done(function(data){
			if(data !=null){
//				alert(data);
				callback(data);
			}
		});
	};
	var getBlackUserList = function(roomName,callback){
		console.log("getBlackUserList");
		//console.log(userInfo);
		
		$.ajax({
			method : "GET",
			url : "/blackUserList",
			contentType : "applcation/json; charset=UTF-8",
			data: {
				roomName : roomName
			}
		}).done(function(data){
			console.log("getBlackUserList done");
			if(callback){
				callback(data);			
			}
		}).fail(function(data) {
			console.log("실패");
		});
	};
	
	return {
		getUserIdx: getUserIdx,
		setUserInfo: setUserInfo,
		setUserId : setUserId,
		enterChatRoom: enterChatRoom,
		exitChatRoom: exitChatRoom,
		updateChatRoom: updateChatRoom,
		getChatRoomList: getChatRoomList,
		getUserList: getUserList,
		getNewEvent: getNewEvent,		
		sendMessage: sendMessage,
		sendAdminMessage: sendAdminMessage,
		sendDirectMessage: sendDirectMessage,
		getBlackList: getBlackList,
		addBlackList: addBlackList,
		removeBlackList: removeBlackList,
		approveMessage: approveMessage,
		rejectMessage: rejectMessage,
		getBeforeMessage : getBeforeMessage,
		getBlackUserList : getBlackUserList
	};
} ();
