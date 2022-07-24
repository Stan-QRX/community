$(function(){
	$(".follow-btn").click(follow);
});

function follow() {
	var btn = this;
	console.log("进入");
	console.log('fianaly'+$("#btnfollow").text());
	if($("#btnfollow").text()=='关注TA') {
		// 关注TA
		$.post(
			CONTEXT_PATH+"/follow",
			{"entityType": 3, "entityId": $("#entityId").val()},
				function (data)
				{
					data=$.parseJSON(data);
					if(data.code==0)
					{
						console.log("关注");
						// 刷新粉丝量
						window.location.reload();
						// $(btn).text("已关注").removeClass("btn-info").addClass("btn-secondary");
						/* 关注数量*/

					}
					else {
						alert(data,msg);
					}
				}
		);

	} else {
		// 取消关注
		$.post(
			CONTEXT_PATH + "/unfollow",
			{"entityType": 3, "entityId": $("#entityId").val()},
			function (data) {
				data = $.parseJSON(data);
				if (data.code == 0) {
					console.log("取关");
					window.location.reload();
					// $(btn).text("关注TA").removeClass("btn-secondary").addClass("btn-info");

				} else {
					alert(data.msg);
				}
			}
		);

	}
}