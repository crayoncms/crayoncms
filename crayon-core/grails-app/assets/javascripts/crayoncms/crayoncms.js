// This is a manifest file that'll be compiled into admin.js.
//
// Any JavaScript file within this directory can be referenced here using a relative path.
//
// You're free to add application-wide JavaScript to this file, but it's generally better
// to create separate JavaScript files as needed.
//
//= require crayoncms/jquery-2.2.0.min
//= require crayoncms/jquery-ui-1.12.0.min
//= require crayoncms/bootstrap
//= require crayoncms/codemirror-5.20.0
//= require crayoncms/codemirror-xml-5.20.0
//= require crayoncms/summernote.min
//= require crayoncms/cropper/imagecrop.min
//= require crayoncms/jquery.mjs.nestedSortable
//= require crayoncms/jquery.ui.touch-punch-0.2.3.min
//= require_tree .
//= require_self

$('.modal').on('shown.bs.modal', function () {
    $(this).removeData('bs.modal');
});

$(document).ajaxStart(function(){
    $("#loading").css("display", "block");
});
$(document).ajaxComplete(function(){
    $("#loading").css("display", "none");
});

$("#slug").attr('readonly', true);

$("#name").keyup(function(){
        var Text = $(this).val();
        Text = Text.toLowerCase();
        Text = Text.replace(/[^a-zA-Z0-9]+/g,'-');
        $("#slug").val(Text);
});

if (typeof jQuery !== 'undefined') {
    (function($) {
        $('#spinner').ajaxStart(function() {
            $(this).fadeIn();
        }).ajaxStop(function() {
            $(this).fadeOut();
        });

        if(document.getElementById("code") != null
            && document.getElementById("code").type === 'textarea') {
            CodeMirror.fromTextArea(document.getElementById("code"), {
                lineNumbers: true,
                mode:  "xml"
            });
        }

        $("#content").summernote({
            height: 400,
            placeholder: "Type here...",
            callbacks: {
                onImageUpload: function(files, editor, welEditable) {
                    for (var i = files.length - 1; i >= 0; i--) {
                        var data = new FormData();
                        data.append("file", files[i]);
                        $.ajax({
                            data: data,
                            type: 'POST',
                            url: '/asset/upload',
                            cache: false,
                            contentType: false,
                            enctype: 'multipart/form-data',
                            processData: false,
                            success: function(data) {
                            	$("#content").summernote('editor.insertImage', data.uri);
                            }
                        });
                    }
                }
            }
        });
        
        
    	$(".menu").nestedSortable({
    		handle: 'div',
    		items: 'li',
    		placeHolder: 'li',
    		toleranceElement: '> div',
    		//listType: 'ul',
			update: function(event, ui) {
				var data =  $(this).nestedSortable('toArray');
				var menuOrder = JSON.stringify(data);
				console.log(menuOrder);
				$.ajax({
					data: 'menuOrder=' + menuOrder, 
					dataType: 'json',
					type: 'POST',
					url: "/admin/menu/order",
					success: function(data, text) {
						console.log(data.retMess);
						console.log(text);
					},
					error: function(xhr, err, errThrown) {
						console.log(errThrown);
					}
				});
			}
    	});

        var img_c;
    	$("#profileFile").change(function() {
            console.log(this.files.length);
    	    if(this.files && this.files[0]) {

    	        var reader = new FileReader();


                var img_d = new ImageCropper('.profilePicWrapper', "img.jpg");
                img_d.destroy();

    	        reader.onload = function(e) {

    	            img_c = new ImageCropper('.profilePicWrapper', e.target.result, {
    	                fixed_size: true,
    	                min_crop_width: 200,
    	                min_crop_height: 200
    	            });
    	        }

    	        reader.readAsDataURL(this.files[0]);
    	    }

    	});

    	$('#uploadProfilePic').click(function() {
    	    console.log("Cropped imaged");
    	    var img_b64_str = img_c.crop("image/jpeg", 1);
    	    console.log(img_b64_str);
    	    var version = $("#version").val();
    	    var id = $("#id").val();
    	    $.ajax({
                data: { 'id': id, 'version': version, 'profilePicture': img_b64_str },
                dataType: 'json',
                type: 'POST',
                url: "/admin/user/changeProfilePic",
                success: function(data, text) {
                    console.log(data.retMess);
                    console.log(text);
                    $("#updateProfilePic").modal("hide");
                },
                error: function(xhr, err, errThrown) {
                    console.log(errThrown);
                }
            });
    	});
    	      
  })(jQuery);
}



