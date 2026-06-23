/**
 * @title : The basic CRUD MESSAGE is the standard, and specific phrases for each page are written and used.
 * @see : COMPLETE : Sentence termination completed
 * FAIL : To signal failure for an action
 * CONFIRM : The termination of the sentence is a question.
 * CHECK : When a certain value does not exist and is requested
 * INFO : Informational message
 * */
// Basic CRUD MESSAGE
const Message = {};
Message.Label = {};
Message.Label.Array = {};


Message.Label.Array["CHECK.ID"] = "Please enter ID";
Message.Label.Array["CHECK.PWD"] = "Please enter password";

Message.Label.Array["COMPLETE.INSERT"] = "Registration completed";
Message.Label.Array["COMPLETE.READ"] = "Search completed";
Message.Label.Array["COMPLETE.UPDATE"] = "Update completed";
Message.Label.Array["COMPLETE.DELETE"] = "Deletion completed";
Message.Label.Array["COMPLETE.PERMIT"] = "Approval completed";
Message.Label.Array["COMPLETE.RETURN"] = "Return completed";
Message.Label.Array["COMPLETE.RETURN_CANCEL"] = "Return cancellation completed";
Message.Label.Array["COMPLETE.CANCEL"] = "Cancellation completed";
Message.Label.Array["COMPLETE.SAVE"] = "Save completed";
Message.Label.Array["COMPLETE.RESEND"] = "Resend completed";

Message.Label.Array["CONFIRM.INSERT"] = "Would you like to register?";
Message.Label.Array["CONFIRM.PERMIT"] = "Would you like to approve?";
Message.Label.Array["CONFIRM.DELETE"] = "Would you like to delete?";
Message.Label.Array["CONFIRM.UPDATE"] = "Would you like to update?";
Message.Label.Array["CONFIRM.RESET"] = "Would you like to reset?";
Message.Label.Array["CONFIRM.CANCEL"] = "Would you like to cancel?";
Message.Label.Array["CONFIRM.RESEND"] = "Would you like to resend?";
Message.Label.Array["CONFIRM.RESET"] = "All entered content will be reset. Would you like to continue?";
Message.Label.Array["CONFIRM.RESET_PWD"] = "Would you like to reset the password?";

Message.Label.Array["FAIL.INSERT"] = "Registration failed";
Message.Label.Array["FAIL.SELECT"] = "Search failed";
Message.Label.Array["FAIL.SELECT_NO_DATA"] = "No data exists";
Message.Label.Array["FAIL.UPDATE"] = "Update failed";
Message.Label.Array["FAIL.DOWNLOAD"] = "Download failed";
Message.Label.Array["FAIL.DELETE"] = "Deletion failed";
Message.Label.Array["FAIL.PERMIT"] = "Approval failed";
Message.Label.Array["FAIL.RETURN"] = "Return failed";
Message.Label.Array["FAIL.RETURN_CANCEL"] = "Return cancellation failed";
Message.Label.Array["FAIL.LOGIN"] = "Login failed";
Message.Label.Array["FAIL.IDPWD"] = "ID or password does not match";
Message.Label.Array["FAIL.REQUIRED"] = "Please fill in all required fields";
Message.Label.Array["FAIL.FUNCTION.POPUP"] = "No popup call method found";
Message.Label.Array["FAIL.NOT.EXIST.DISABLED"] = "data-disabled tag is not set";
Message.Label.Array["FAIL.SAME.PWD"] = "The new password is the same as the current password";
Message.Label.Array["FAIL.NOT.MATCHED.PWD"] = "The current password does not match";
Message.Label.Array["FAIL.COMPARE.PWD"] = "Passwords do not match.";
Message.Label.Array["FAIL.UPLOAD"] = "Upload failed";
Message.Label.Array["FAIL.UPLOAD.SIZE_EXCEEDED"] = "File upload capacity exceeded";
Message.Label.Array["FAIL.ACCESS_DENIED"] = "Access denied.";


Message.Label.Array["CHECK.FORMTYPE"] = "Please enter in the correct format";
Message.Label.Array["CHECK.DATA_ATTRIBUTE"] = "Please re-check the tag attributes";
Message.Label.Array["CHECK.MATCH.PWD"] = "Please enter your current password";
Message.Label.Array["CHECK.INPUT.PWD"] = "Please enter password";
Message.Label.Array["CHECK.INPUT.PWD_CHECK"] = "Please enter password confirmation";

Message.Label.Array["BEFORE_PWD"] = "Current Password";

Message.Label.Array["ALERT.NO.CHANGED.FILE"] = "No changes made";
Message.Label.Array["CONFIRM.CLOSE_UNSAVED_CHANGES"] = "You have unsaved changes. Would you like to close the window?";

Message.Label.Array["BACK_BTN"] = "Go Back";
Message.Label.Array["CLOSE_BTN"] = "Close";
Message.Label.Array["CANCEL_BTN"] = "Cancel";
Message.Label.Array["DELETE_BTN"] = "Delete";
Message.Label.Array["DETAIL_BTN"] = "Details";
Message.Label.Array["UPDATE_BTN"] = "Edit";
Message.Label.Array["MORE_BTN"] = "More";
Message.Label.Array["SAVE_BTN"] = "Save";
Message.Label.Array["SELECT_BTN"] = "Select";
Message.Label.Array["SEARCH_BTN"] = "Search";
Message.Label.Array["SEARCH_KEYWORD"] = "Keyword";
Message.Label.Array["REGISTER_BTN"] = "Register";
Message.Label.Array["RESET_BTN"] = "Reset";
Message.Label.Array["UPLOAD_BTN"] = "Upload";
Message.Label.Array["SIDE_BTN"] = "Side";
Message.Label.Array["RESET_PWD_BTN"] = "Reset Password";
Message.Label.Array["REDIRECT_BTN"] = "Move";
Message.Label.Array["WRITE_BTN"] = "Write";
Message.Label.Array["CONFIRM_BTN"] = "Confirm";


Message.Label.Array["TOTAL"] = "Total";
Message.Label.Array["CASE"] = "cases";
Message.Label.Array["ALL"] = "All";
Message.Label.Array["GALLERY"] = "Gallery";
Message.Label.Array["NOTICE"] = "Notice";
Message.Label.Array["SEARCH_PLACEHOLDER"] = "Please enter search keyword";
Message.Label.Array["NO_TITLE"] = "No title";
Message.Label.Array["UNKNOWN"] = "Unknown";
Message.Label.Array["NO"] = "No";

Message.Label.Array["START_DATE"] = "Start Date";
Message.Label.Array["END_DATE"] = "End Date";


Message.Label.Array["FAIL.DELETE.REASON.EXIST_CHILD_DATA"] = "Deletion failed because sub-data exists";
Message.Label.Array["MAXIMUM.UPLOAD.SIZE.100MB"] = "File upload capacity cannot exceed 100 MB";
Message.Label.Array["ALERT.NO.FILE"] = "No file selected";
Message.Label.Array["FILE_DOWN_BTN"] = "Download Attached File";
Message.Label.Array["FILE_ATTACH"] = "File Attachment";
Message.Label.Array["FILE_ATTACH_INFO"] = "Attached files can be checked below.";
Message.Label.Array["FILE_ATTACH_AREA_NOT_EXIST_FILE"] = "No attached files.";
Message.Label.Array["FILE_REGISTER_BTN"] = "Add File";
Message.Label.Array["FILE_NM"] = "File Name";
Message.Label.Array["FILE_SZ"] = "File Size";
Message.Label.Array["FILE_EXT"] = "Extension";
Message.Label.Array["FILE_DESCRIPTION"] = "Description";

Message.Label.Array["USE_YN"] = "Usage Status";
Message.Label.Array["NOT_USED"] = "Inactive";
Message.Label.Array["USED"] = "Active";

Message.Label.Array["LOGIN"] = "Login";
Message.Label.Array["EMAIL"] = "Email";
Message.Label.Array["PWD"] = "Password";
Message.Label.Array["REMEMBER_ME"] = "Remember Email";

// LOGIN PAGE
Message.Label.Array["LOGIN.KEEP_LOGGED_IN"] = "Keep Logged In";
Message.Label.Array["LOGIN.FORGOT_PWD"] = "Forgot Password";
Message.Label.Array["LOGIN.RESET_PWD_TITLE"] = "Reset Password";
Message.Label.Array["LOGIN.SEND_VERIFICATION_CODE"] = "Send Verification Code";
Message.Label.Array["LOGIN.VERIFICATION_CODE"] = "Verification Code";
Message.Label.Array["LOGIN.CONFIRM_VERIFICATION_CODE"] = "Verify Code";
Message.Label.Array["LOGIN.NEW_PWD"] = "New Password";
Message.Label.Array["LOGIN.PWD_POLICY_GUIDE"] = "Combination of 4-12 alphanumeric characters and special characters";
Message.Label.Array["LOGIN.NEW_PWD_CONFIRM"] = "Confirm New Password";
Message.Label.Array["LOGIN.PWD_CONFIRM"] = "Confirm Password";
Message.Label.Array["LOGIN.INITIALIZE_PWD_BTN"] = "Initialize Password";
Message.Label.Array["LOGIN.APPLY_BTN"] = "Apply";
Message.Label.Array["LOGIN.CLOSE_BTN"] = "Close";
Message.Label.Array["LOGIN.MAIL_TITLE_VERIFICATION"] = "[giens] Verification Code Sent";
Message.Label.Array["LOGIN.FAIL_SEND_VERIFICATION_CODE"] = "Failed to send verification code";
Message.Label.Array["LOGIN.EXP_VERIFICATION_CODE"] = "Verification code has expired.";
Message.Label.Array["LOGIN.MAIL_CONTENT_HEADER"] = "[giens] Email Verification Code";
Message.Label.Array["LOGIN.MAIL_CONTENT_BODY1"] = "We have sent a verification code to reset your password.";
Message.Label.Array["LOGIN.MAIL_CONTENT_BODY2"] = "Please enter the verification code below.";
Message.Label.Array["LOGIN.COMPLETE_REGISTER"] = "Registration completed";
Message.Label.Array["LOGIN.INPUT_VERIFICATION_CODE"] = "Please enter the verification code.";
Message.Label.Array["LOGIN.NEW_PWD"] = "New Password";
Message.Label.Array["LOGIN.NEW_PWD_CONFIRM"] = "Confirm New Password";
Message.Label.Array["LOGIN.RESET_PWD_PAGE_GUIDE"] = "Please reset your password.";
Message.Label.Array["LOGIN.RESET_PWD_INVALID_ACCESS"] = "Invalid access. Please try the password reset again.";
Message.Label.Array["CHECK.INPUT.EMAIL"] = "Please enter your email";
