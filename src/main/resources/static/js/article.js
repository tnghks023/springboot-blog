//delete
const deleteButton = document.getElementById('delete-btn');

if(deleteButton) {
    deleteButton.addEventListener('click', (event) => {
        let id = document.getElementById('article-id').value;
        function success() {
            alert("삭제가 완료되었습니다.");
            location.replace("/articles");
        }

        function fail() {
            alert("삭제 실패했습니다.");
            location.replace("/articles")
        }

        httpRequest("DELETE", "/api/articles/" + id, null, success, fail);
        });
}

//modify
const modifyButton = document.getElementById('modify-btn');

if(modifyButton) {
    modifyButton.addEventListener('click', event => {
    //location.search : ?foo=1&bar=2"
    // var url = new URL("https://example.com?foo=1&bar=2"); params.get("foo")
    let params = new URLSearchParams(location.search);
    let id = params.get('id');

    body = JSON.stringify({
        title : document.getElementById("title").value,
        content : document.getElementById("content").value,
    });

    function success() {
        alert("수정 완료되었습니다.");
        location.replace("/articles/" + id);
    }

    function fail() {
        alert("수정 실패했습니다.");
        location.replace("/articles/" + id);
    }
    httpRequest("PUT", "/api/articles/" + id, body, success, fail);
    });
}

//create
const createButton = document.getElementById("create-btn");
if(createButton) {
// 등록 버튼을 클릭하면 /api/articles 로 요청을 보냄
    createButton.addEventListener("click", (event) => {
        body = JSON.stringify({
            title: document.getElementById("title").value,
            content: document.getElementById("content").value
        });
        function success() {
            alert("등록이 완료되었습니다.");
            location.replace("/articles");
        }
        function fail() {
            alert("등록 실패했습니다.");
            location.replace("/articles")
        }

        httpRequest("POST", "/api/articles", body, success, fail);
    });
}

function getCookie(key) {
    var result = null;
    var cookie = document.cookie.split(";");
    cookie.some(function (item) {
        item = item.replace(" ", "");

        var dic = item.split("=");

        if(key === dic[0]) {
            result = dic[1];
            return true;
        }
    });

    return result;
}

function deleteCookie(name) {
    document.cookie = name + '=; expires=Thu, 01, Jan 1970 00:00:01 GMT;';
}

//like btn
const likeBtn = document.getElementById("like-btn");
if(likeBtn) {

    likeBtn.addEventListener("click", (event) => {
    let id = document.getElementById('article-id').value;
        body = JSON.stringify({
            articleId: id
        });
        function success() {
            alert("좋아요를 완료했습니다..");
           let likeCount = document.getElementById('like-cnt');
            likeCount.textContent = parseInt(likeCount.textContent) + 1;
            // Change the button style and text
            likeBtn.textContent = 'Liked!';
            likeBtn.classList.remove('btn-primary');
            likeBtn.classList.add('btn-danger');
        }
        function fail() {
            alert("등록 실패했습니다.");
        }

        httpRequest("PUT", "/articles/"+id+"/like", body, success, fail);
    });
}

// HTTP 요청을 보내는 함수
function httpRequest(method, url, body, success, fail) {
    fetch(url, {
        method : method,
        headers : {
            Authorization: "Bearer " + localStorage.getItem("access_token"),
            "Content-Type": "application/json",
        },
        body : body,
    }).then((response) => {
        if( response.status === 200 || response.status === 201) {
            return success();
        }
        const refresh_token = getCookie("refresh_token");
        if(response.status === 401 && refresh_token) {
            fetch("/api/token", {
                method : "POST",
                headers : {
                    Authorization: "Bearer " + localStorage.getItem("access_token"),
                    "Content-Type": "application/json",
                },
                body : JSON.stringify({
                    refreshToken: getCookie("refresh_token"),
                }),
            }).then((res) => {
                if(res.ok) {
                    return res.json();
                }
            }).then((result) => {
                // 재발급이 성공하면 로컬 스토리지값을 새로운 액세스 토큰으로 교체
                localStorage.setItem("access_token", result.accessToken);
                httpRequest(method, url, body, success, fail);
            })
            .catch((error) => fail());
        } else {
            return fail();
        }
    });
}

// 로그아웃
const logoutBtn = document.getElementById("logout-btn");

if(logoutBtn) {
    logoutBtn.addEventListener("click", event => {

        function success() {
            alert("로그아웃이 완료되었습니다.");
            localStorage.removeItem("access_token");
            deleteCookie("refresh_token");

            location.replace("/login");
        }

        function fail() {
            alert("로그아웃이 실패했습니다.");
        }

        httpRequest('DELETE', '/api/refresh-token' , null, success, fail);
    });
}


// 댓글 생성
const commentCreateButton = document.getElementById('comment-create-btn');

if(commentCreateButton) {
    commentCreateButton.addEventListener('click', event => {
        articleId = document.getElementById('article-id').value;

        body = JSON.stringify({
            articleId : articleId,
            content : document.getElementById('content').value
        });
        function success() {
            alert('등록이 완료되었습니다.');
            location.replace('/articles/'+articleId);
        };
        function fail() {
            alert('등록에 실패했습니다.');
            location.replace('/articles/'+articleId);
        }

        httpRequest('POST', '/api/comments', body, success, fail)
    });
}

// 댓글 삭제
document.querySelectorAll('.comment-delete-btn').forEach(button => {
     button.addEventListener('click', event => {
            commentId = document.getElementById('comment-id').value;
            articleId = document.getElementById('article-id').value;

            function success() {
                alert('댓글삭제가 완료되었습니다.');
                location.replace('/articles/'+articleId);
            };
            function fail() {
                alert('댓글삭제에 실패했습니다.');
            }

            httpRequest('DELETE', '/api/comments/' + commentId, null, success, fail)
     });
})

// 댓글 수정
document.querySelectorAll('.comment-modify-pop-btn').forEach(button => {

    button.addEventListener('click', (event) => {
        const section = event.target.closest('section');
        const cardBody = section.querySelector('.card-body');
        const commentText = cardBody.querySelector('.card-text').textContent;

        // Remove existing text area and modify button if they already exist

        let textArea = null;

        const existingTextArea = section.querySelector('textarea');
        if (existingTextArea) {
            existingTextArea.remove();
        } else {
            // Create and insert text area
            textArea = document.createElement('textarea');
            textArea.className = 'form-control mb-2 mt-2';
            textArea.value = commentText;
            section.appendChild(textArea);
        }

        const existingModifyButton = section.querySelector('.modify-btn');

        if (existingModifyButton) {
            existingModifyButton.remove();
        } else {
            // Create and insert modify button
            const modifyButton = document.createElement('button');
            modifyButton.className = 'modify-btn btn btn-success btn-sm';
            modifyButton.textContent = 'Save';
            section.appendChild(modifyButton);

            modifyButton.addEventListener('click', () => {
                const updatedText = textArea.value;

                commentId = document.getElementById('comment-id').value;
                articleId = document.getElementById('article-id').value;

                body = JSON.stringify({
                    content : textArea.value
                });

                function success() {
                    alert('댓글수정이 완료되었습니다.');
                    location.replace('/articles/'+articleId);
                };
                function fail() {
                    alert('댓글수정에 실패했습니다.');
                }

                httpRequest('PUT', '/api/comments/' + commentId, body, success, fail)
            });
        }
    })
})

