/**
 * This file Copyright (c) 2010 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.module.genuinecentral.gwt.client;

//import info.magnolia.module.rest.dialog.Dialog;
import info.magnolia.module.genuinecentral.dialog.DialogJSO;
import info.magnolia.module.genuinecentral.dialog.DialogJSOImpl;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import static com.google.gwt.http.client.RequestBuilder.GET;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import static com.google.gwt.http.client.Response.SC_NO_CONTENT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Vivian Steller
 * @since 1.0.0
 */
public class DialogRegistryClient {

//    public void getDialog(final String dialogName, final AsyncCallback<Dialog> callback) {
//        final RequestBuilder request = new RequestBuilder(GET, "/.rest/dialogs/" + dialogName + "?mgnlUserId=superuser&mgnlUserPSWD=superuser");
//        request.setHeader("Accept", "application/json");
//
//        request.setCallback(new com.google.gwt.http.client.RequestCallback() {
//            public void onResponseReceived(Request request, Response response) {
//                try {
//                    if (response.getStatusCode() == SC_NO_CONTENT) {
//                        callback.onFailure(new Exception("No such dialog: " + dialogName));
//                        return;
//                    }
//
//                    // populate the dialog with the data from the response
//                    Dialog dialog = buildDialogFrom(response);
//
//                    // call the callback
//                    callback.onSuccess(dialog);
//                } catch (Exception e) {
//                    callback.onFailure(new Exception("Failed to load dialog: " + e.getMessage(), e));
//                }
//            }
//
//            public void onError(Request request, Throwable exception) {
//                // dispatch the exception
//                callback.onFailure(new Exception("Failed to load dialog: " + exception.getMessage(), exception));
//            }
//        });
//
//        try {
//            request.send();
//        } catch (RequestException e) {
//            Window.alert("Request error: " + e.getMessage());
//        }
//    }
//
//    private Dialog buildDialogFrom(Response response) {
////      final String jsonString = "{\"dialog\":{\"controls\":[{\"label\":\"My Textfield\",\"type\":\"textfield\"}, {\"label\":\"My Textfield\",\"type\":\"textfield\"}],\"label\":\"My Custom Dialog\"}}";
////        final String jsonString = "{\"dialog\":{\"controls\":{\"label\":\"My Textfield\",\"type\":\"textfield\"},\"label\":\"My Custom Dialog\"}}";
//        final DialogWrapperJSO dialogWrapperJSO = (DialogWrapperJSO) evaluate(response.getText());
//        final DialogJSO dialogJSO = dialogWrapperJSO.getDialog();
//
//        return new DialogJSOImpl(dialogJSO);
//    }
//
//    private native JavaScriptObject evaluate(String jsonString) /*-{
//      return eval('(' + jsonString + ')');
//   }-*/;
//
//    public static abstract class RequestCallback {
//        public abstract void onDialogReceived(Dialog dialog);
//
//        public abstract void onDialogError(String message);
//    }

    public static class DialogWrapperJSO extends JavaScriptObject {
        protected DialogWrapperJSO() {
        }

        public final native DialogJSO getDialog() /*-{
            return this.dialog;
        }-*/;
    }

}
