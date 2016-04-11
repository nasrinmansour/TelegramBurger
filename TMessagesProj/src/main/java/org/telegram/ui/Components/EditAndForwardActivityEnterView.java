package org.telegram.ui.Components;

import android.app.Activity;

import org.telegram.messenger.SendMessagesHelper;
import org.telegram.ui.ActionBar.BaseFragment;

/**
 * Created by Morteza on 2016/04/03.
 */
public class EditAndForwardActivityEnterView extends ChatActivityEnterView {

    public EditAndForwardActivityEnterView(Activity context, SizeNotifierFrameLayout parent, BaseFragment fragment, boolean isChat) {
        super(context, parent, fragment, isChat);
    }

    @Override
    protected void sendMessage() {
//        if (delegate != null) {
//            delegate.onMessageSend(getFieldText());
//        }
        String message = ((EditTextCaption)messageEditText).getText().toString();
        if (processSendingText(message)) {
//            messageEditText.setText("");
            if (delegate != null) {
                delegate.onMessageSend(message);
            }
        }
    }

    @Override
    public boolean processSendingText(String text) {
        text = getTrimmedString(text);
        if (text.length() != 0) {
            int count = (int) Math.ceil(text.length() / 4096.0f);
            for (int a = 0; a < count; a++) {
                String mess = text.substring(a * 4096, Math.min((a + 1) * 4096, text.length()));

            }
            return true;
        }
        return false;
    }
}
