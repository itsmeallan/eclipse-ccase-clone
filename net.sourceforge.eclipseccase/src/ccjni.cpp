#include "net_sourceforge_eclipseccase_ClearcaseJNI.h"
#include <iostream>
#include <sstream>
using namespace std;

#import <ccauto.dll> named_guids 
using namespace ClearCase;

_bstr_t formatError(_com_error& cerror)
{
	ostringstream os;
	os << "Error: " << cerror.ErrorMessage() << endl;
	os << "Details: " << (const char*) cerror.Description() << endl;
	return _bstr_t(os.str().c_str());
}

void raiseJNIException(JNIEnv * env, const char * msg)
{
	jclass newExcCls = env->FindClass("java/lang/Exception");
	env->ThrowNew(newExcCls, msg);
}

jobject createStatus(JNIEnv * env, boolean status, const char * message)
{
	jclass statusClass = env->FindClass("net/sourceforge/eclipseccase/IClearcase$Status");
	jmethodID statusCtor = NULL;
	if (statusClass)
		statusCtor = env->GetMethodID(statusClass, "<init>", "(ZLjava/lang/String;)V");
	else
		raiseJNIException(env, "Could get Status class in Clearcase JNI layer");
	if (statusCtor == NULL)
		raiseJNIException(env, "Could get Status constructor in Clearcase JNI layer");

	jobject statusObj = env->NewObject(statusClass, statusCtor, status, env->NewStringUTF(message));
	return statusObj;
}

#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     net_sourceforge_eclipseccase_ClearcaseJNI
 * Method:    initialize
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_net_sourceforge_eclipseccase_ClearcaseJNI_initialize
  (JNIEnv * env, jclass obj)
{
	IClearCasePtr ccase = NULL;
	IClearToolPtr cleartool = NULL;

	try 
	{ 
		CoInitialize(NULL);
		ccase = IClearCasePtr(CLSID_Application);
		cleartool = IClearToolPtr(CLSID_ClearTool);
	}
	catch(_com_error& cerror) 
	{ 
		ostringstream os;
		os << (const char *) cerror.Description(); 
		os << "Error code: " << cerror.Error(); 
		raiseJNIException(env, os.str().c_str());
	}
	catch(...)
	{
		raiseJNIException(env, "Unhandled Exception in Clearcase JNI layer");
	}
	if (ccase == NULL)
		raiseJNIException(env, "Could not get a IClearCasePtr instance in Clearcase JNI layer");
	if (cleartool == NULL)
		raiseJNIException(env, "Could not get a IClearToolPtr instance in Clearcase JNI layer");
}

/*
 * Class:     net_sourceforge_eclipseccase_ClearcaseJNI
 * Method:    checkout
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jobject JNICALL Java_net_sourceforge_eclipseccase_ClearcaseJNI_jnicheckout
  (JNIEnv * env, jclass obj, jstring file, jstring comment, jboolean reserved, jboolean ptime)
{
	jobject result = NULL;
	const char *filestr = env->GetStringUTFChars(file, 0);
	const char *commentstr = env->GetStringUTFChars(comment, 0);
	try 
	{ 
		CoInitialize(NULL);
		IClearCasePtr ccase = IClearCasePtr(CLSID_Application);
		ICCVersionPtr ver = ccase->GetVersion(filestr);
		boolean useHijacked = false;
		boolean mustBeLatest = false;
		ICCCheckedOutFilePtr cofile = ver->CheckOut(reserved ? ccReserved : ccUnreserved,
																  commentstr,
																  useHijacked,
																  ccVersion_Default,
																  mustBeLatest,
																  ptime);
		result = createStatus(env, true, "Checkout Successful");
	}
	catch(_com_error& cerror) 
	{ 
		result = createStatus(env, false, formatError(cerror));
	}
	catch(...)
	{
		raiseJNIException(env, "Unhandled Exception in Clearcase JNI layer");
	}

	env->ReleaseStringUTFChars(file, filestr);
	env->ReleaseStringUTFChars(comment, commentstr);
	return result;
}

/*
 * Class:     net_sourceforge_eclipseccase_ClearcaseJNI
 * Method:    checkin
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jobject JNICALL Java_net_sourceforge_eclipseccase_ClearcaseJNI_jnicheckin
  (JNIEnv * env, jclass obj, jstring file, jstring comment, jboolean ptime)
{
	jobject result = NULL;
	const char *filestr = env->GetStringUTFChars(file, 0);
	const char *commentstr = env->GetStringUTFChars(comment, 0);

	try 
	{ 
		CoInitialize(NULL);
		IClearCasePtr ccase = IClearCasePtr(CLSID_Application);
		ICCVersionPtr ver = ccase->GetVersion(filestr);
		ICCCheckedOutFilePtr cofile = ccase->GetCheckedOutFile(filestr);
		cofile->CheckIn(commentstr, ptime, "", ccRemove);
		result = createStatus(env, true, "Checkin Successful");
	}
	catch(_com_error& cerror) 
	{ 
		result = createStatus(env, false, formatError(cerror));
	}
	catch(...)
	{
		raiseJNIException(env, "Unhandled Exception in Clearcase JNI layer");
	}

	env->ReleaseStringUTFChars(file, filestr);
	env->ReleaseStringUTFChars(comment, commentstr);
	return result;
}

/*
 * Class:     net_sourceforge_eclipseccase_ClearcaseJNI
 * Method:    uncheckout
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jobject JNICALL Java_net_sourceforge_eclipseccase_ClearcaseJNI_jniuncheckout
  (JNIEnv * env, jclass obj, jstring file, jboolean keep)
{
	jobject result = NULL;
	const char *filestr = env->GetStringUTFChars(file, 0);

	try 
	{ 
		CoInitialize(NULL);
		IClearCasePtr ccase = IClearCasePtr(CLSID_Application);
		ICCVersionPtr ver = ccase->GetVersion(filestr);
		ICCCheckedOutFilePtr cofile = ccase->GetCheckedOutFile(filestr);
		cofile->UnCheckOut(keep ? ccKeep : ccRemove);
		result = createStatus(env, true, "Uncheckout Successful");
	}
	catch(_com_error& cerror) 
	{ 
		result = createStatus(env, false, formatError(cerror));
	}
	catch(...)
	{
		raiseJNIException(env, "Unhandled Exception in Clearcase JNI layer");
	}

	env->ReleaseStringUTFChars(file, filestr);
	return result;
}

/*
 * Class:     net_sourceforge_eclipseccase_ClearcaseJNI
 * Method:    delete
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jobject JNICALL Java_net_sourceforge_eclipseccase_ClearcaseJNI_jnidelete
  (JNIEnv * env, jclass obj, jstring file, jstring comment)
{
	jobject result = NULL;
	const char *filestr = env->GetStringUTFChars(file, 0);
	const char *commentstr = env->GetStringUTFChars(comment, 0);

	try 
	{ 
		CoInitialize(NULL);
		IClearCasePtr ccase = IClearCasePtr(CLSID_Application);
		ICCVersionPtr ver = ccase->GetVersion(filestr);
		ICCElementPtr elt = ver->GetElement();
		elt->RemoveName(commentstr, true);
		result = createStatus(env, true, "Delete Successful");
	}
	catch(_com_error& cerror) 
	{ 
		result = createStatus(env, false, formatError(cerror));
	}
	catch(...)
	{
		raiseJNIException(env, "Unhandled Exception in Clearcase JNI layer");
	}

	env->ReleaseStringUTFChars(file, filestr);
	env->ReleaseStringUTFChars(comment, commentstr);
	return result;
}

/*
 * Class:     net_sourceforge_eclipseccase_ClearcaseJNI
 * Method:    add
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jobject JNICALL Java_net_sourceforge_eclipseccase_ClearcaseJNI_jniadd
  (JNIEnv * env, jclass obj, jstring file, jstring comment, jboolean isdirectory)
{
	jobject result = NULL;
	const char *filestr = env->GetStringUTFChars(file, 0);
	const char *commentstr = env->GetStringUTFChars(comment, 0);

	try 
	{ 
		CoInitialize(NULL);
		IClearCasePtr ccase = IClearCasePtr(CLSID_Application);
		ICCCheckedOutFilePtr cofile;
		if (isdirectory)
			cofile = ccase->CreateElement(filestr, commentstr, false, "directory");
		else
			cofile = ccase->CreateElement(filestr, commentstr, false);
		result = createStatus(env, true, "Add Successful");
	}
	catch(_com_error& cerror) 
	{ 
		result = createStatus(env, false, formatError(cerror));
	}
	catch(...)
	{
		raiseJNIException(env, "Unhandled Exception in Clearcase JNI layer");
	}

	env->ReleaseStringUTFChars(file, filestr);
	env->ReleaseStringUTFChars(comment, commentstr);
	return result;
}

/*
 * Class:     net_sourceforge_eclipseccase_ClearcaseJNI
 * Method:    rename
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Lorg/eclipse/team/clearcase/jni/Clearcase$Status;
 */
JNIEXPORT jobject JNICALL Java_net_sourceforge_eclipseccase_ClearcaseJNI_jnimove
  (JNIEnv * env, jclass obj, jstring file, jstring newfile, jstring comment)
{
	jobject result = NULL;
	const char *filestr = env->GetStringUTFChars(file, 0);
	const char *newfilestr = env->GetStringUTFChars(newfile, 0);
	const char *commentstr = env->GetStringUTFChars(comment, 0);

	try 
	{ 
		CoInitialize(NULL);
		IClearCasePtr ccase = IClearCasePtr(CLSID_Application);
		ICCElementPtr elt = ccase->GetElement(filestr);
		HRESULT ret = elt->Rename(newfilestr, commentstr);
		if (ret)
		{
			ostringstream msg;
			msg << "Move failed with the following code: " << ret << ends;
			result = createStatus(env, false, msg.str().c_str());
		}
		else
		{
			result = createStatus(env, true, "Move Successful");
		}
	}
	catch(_com_error& cerror) 
	{ 
		result = createStatus(env, false, formatError(cerror));
	}
	catch(...)
	{
		raiseJNIException(env, "Unhandled Exception in Clearcase JNI layer");
	}

	env->ReleaseStringUTFChars(file, filestr);
	env->ReleaseStringUTFChars(newfile, newfilestr);
	env->ReleaseStringUTFChars(comment, commentstr);
	return result;
}

/*
 * Class:     net_sourceforge_eclipseccase_ClearcaseJNI
 * Method:    getViewName
 * Signature: (Ljava/lang/String;)Lorg/eclipse/team/clearcase/jni/Clearcase$Status;
 */
JNIEXPORT jobject JNICALL Java_net_sourceforge_eclipseccase_ClearcaseJNI_jnigetViewName
  (JNIEnv * env, jclass obj, jstring file)
{
	jobject result = NULL;
	const char *filestr = env->GetStringUTFChars(file, 0);

	try 
	{ 
		CoInitialize(NULL);
		IClearCasePtr ccase = IClearCasePtr(CLSID_Application);
		ICCViewPtr view = ccase->GetView(filestr);
		result = createStatus(env, true, view->GetTagName());
	}
	catch(_com_error& cerror) 
	{ 
		result = createStatus(env, false, formatError(cerror));
	}
	catch(...)
	{
		raiseJNIException(env, "Unhandled Exception in Clearcase JNI layer");
	}

	env->ReleaseStringUTFChars(file, filestr);
	return result;
}

/*
 * Class:     net_sourceforge_eclipseccase_ClearcaseJNI
 * Method:    cleartool
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jobject JNICALL Java_net_sourceforge_eclipseccase_ClearcaseJNI_jnicleartool
  (JNIEnv * env, jclass obj, jstring cmd)
{
	jobject result = NULL;
	const char *cmdstr = env->GetStringUTFChars(cmd, 0);
	
	try 
	{
		// Issue a ClearTool command 
		// Cleartool requires a COInitialize everytime for some reason.
		CoInitialize(NULL);
		IClearToolPtr cleartool = IClearToolPtr(CLSID_ClearTool);
		_bstr_t output = cleartool->CmdExec(cmdstr);
		if (!output)
			output = "";
		result = createStatus(env, true, (const char*) output);
	}
	catch(_com_error& cerror) 
	{ 
		result = createStatus(env, false, formatError(cerror));
	}
	catch(...)
	{
		raiseJNIException(env, "Unhandled Exception in Clearcase JNI layer");
	}

	env->ReleaseStringUTFChars(cmd, cmdstr);
	return result;

}

/*
 * Class:     net_sourceforge_eclipseccase_ClearcaseJNI
 * Method:    isCheckedOut
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_net_sourceforge_eclipseccase_ClearcaseJNI_jniisCheckedOut
  (JNIEnv * env, jclass obj, jstring file)
{
	boolean result = false;
	const char *filestr = env->GetStringUTFChars(file, 0);

	try 
	{ 
		CoInitialize(NULL);
		IClearCasePtr ccase = IClearCasePtr(CLSID_Application);
		ICCVersionPtr ver = ccase->GetVersion(filestr);
		result = ver->GetIsCheckedOut();
	}
	catch(_com_error& cerror) 
	{ 
		raiseJNIException(env, formatError(cerror));
	}
	catch(...)
	{
		raiseJNIException(env, "Unhandled Exception in Clearcase JNI layer");
	}

	env->ReleaseStringUTFChars(file, filestr);
	return result;
}

/*
 * Class:     net_sourceforge_eclipseccase_ClearcaseJNI
 * Method:    isElement
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_net_sourceforge_eclipseccase_ClearcaseJNI_jniisElement
  (JNIEnv * env, jclass obj, jstring file)
{
	boolean result = false;
	const char *filestr = env->GetStringUTFChars(file, 0);

	try 
	{ 
		CoInitialize(NULL);
		IClearCasePtr ccase = IClearCasePtr(CLSID_Application);
		ICCVersionPtr ver = ccase->GetVersion(filestr);
		result = true;
	}
	catch(_com_error&) 
	{ 
	}
	catch(...)
	{
		raiseJNIException(env, "Unhandled Exception in Clearcase JNI layer");
	}

	env->ReleaseStringUTFChars(file, filestr);
	return result;
}

/*
 * Class:     net_sourceforge_eclipseccase_ClearcaseJNI
 * Method:    isDifferent
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_net_sourceforge_eclipseccase_ClearcaseJNI_jniisDifferent
  (JNIEnv * env, jclass obj, jstring file)
{
	boolean result = false;
	const char *filestr = env->GetStringUTFChars(file, 0);

	try 
	{ 
		CoInitialize(NULL);
		IClearCasePtr ccase = IClearCasePtr(CLSID_Application);
		ICCVersionPtr ver = ccase->GetVersion(filestr);
		result = ver->GetIsDifferent();
	}
	catch(_com_error& cerror) 
	{ 
		raiseJNIException(env, formatError(cerror));
	}
	catch(...)
	{
		raiseJNIException(env, "Unhandled Exception in Clearcase JNI layer");
	}

	env->ReleaseStringUTFChars(file, filestr);
	return result;
}

/*
 * Class:     net_sourceforge_eclipseccase_ClearcaseJNI
 * Method:    isSnapShot
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_net_sourceforge_eclipseccase_ClearcaseJNI_jniisSnapShot
  (JNIEnv * env, jclass obj, jstring file)
{
	boolean result = false;
	const char *filestr = env->GetStringUTFChars(file, 0);

	try 
	{ 
		CoInitialize(NULL);
		IClearCasePtr ccase = IClearCasePtr(CLSID_Application);
		ICCViewPtr view = ccase->GetView(filestr);
		result = view->GetIsSnapShot();
	}
	catch(_com_error& cerror) 
	{ 
		raiseJNIException(env, formatError(cerror));
	}
	catch(...)
	{
		raiseJNIException(env, "Unhandled Exception in Clearcase JNI layer");
	}

	env->ReleaseStringUTFChars(file, filestr);
	return result;
}

/*
 * Class:     net_sourceforge_eclipseccase_ClearcaseJNI
 * Method:    isHijacked
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_net_sourceforge_eclipseccase_ClearcaseJNI_jniisHijacked
  (JNIEnv * env, jclass obj, jstring file)
{
	boolean result = false;
	const char *filestr = env->GetStringUTFChars(file, 0);

	try 
	{ 
		CoInitialize(NULL);
		IClearCasePtr ccase = IClearCasePtr(CLSID_Application);
		ICCVersionPtr ver = ccase->GetVersion(filestr);
		result = ver->GetIsHijacked();
	}
	catch(_com_error& cerror) 
	{ 
		raiseJNIException(env, formatError(cerror));
	}
	catch(...)
	{
		raiseJNIException(env, "Unhandled Exception in Clearcase JNI layer");
	}

	env->ReleaseStringUTFChars(file, filestr);
	return result;
}

#ifdef __cplusplus
}
#endif

