#include "net_sourceforge_eclipseccase_jni_Clearcase.h"
#include <iostream>
#include <sstream>
using namespace std;

#import <ccauto.dll> named_guids 
using namespace ClearCase;

static IClearCasePtr ccase = NULL;
static IClearToolPtr cleartool = NULL;

#ifdef __cplusplus
extern "C" {
#endif


void raiseJNIException(JNIEnv * env, const char * msg)
{
	jclass newExcCls = env->FindClass("java/lang/Exception");
	env->ThrowNew(newExcCls, msg);
}

jobject createStatus(JNIEnv * env, boolean status, const char * message)
{
	jclass statusClass = env->FindClass("net/sourceforge/eclipseccase/jni/Clearcase$Status");
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

/*
 * Class:     net_sourceforge_eclipseccase_jni_Clearcase
 * Method:    initialize
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_net_sourceforge_eclipseccase_jni_Clearcase_initialize
  (JNIEnv * env, jclass obj)
{
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
	if(ccase == NULL)
		raiseJNIException(env, "Could not initialize Clearcase in JNI layer");
}

/*
 * Class:     net_sourceforge_eclipseccase_jni_Clearcase
 * Method:    checkout
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jobject JNICALL Java_net_sourceforge_eclipseccase_jni_Clearcase_checkout
  (JNIEnv * env, jclass obj, jstring file, jstring comment, jboolean reserved)
{
	jobject result = NULL;
	const char *filestr = env->GetStringUTFChars(file, 0);
	const char *commentstr = env->GetStringUTFChars(comment, 0);
	try 
	{ 
		ICCVersionPtr ver = ccase->GetVersion(filestr);
		ICCCheckedOutFilePtr cofile = ver->CheckOut(reserved ? ccReserved : ccUnreserved,
																  commentstr,
																  false, ccVersion_Default, false, false);
		result = createStatus(env, true, "Checkout Successful");
	}
	catch(_com_error& cerror) 
	{ 
		result = createStatus(env, false, cerror.Description());
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
 * Class:     net_sourceforge_eclipseccase_jni_Clearcase
 * Method:    checkin
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jobject JNICALL Java_net_sourceforge_eclipseccase_jni_Clearcase_checkin
  (JNIEnv * env, jclass obj, jstring file, jstring comment)
{
	jobject result = NULL;
	const char *filestr = env->GetStringUTFChars(file, 0);
	const char *commentstr = env->GetStringUTFChars(comment, 0);

	try 
	{ 
		ICCVersionPtr ver = ccase->GetVersion(filestr);
		ICCCheckedOutFilePtr cofile = ccase->GetCheckedOutFile(filestr);
		cofile->CheckIn(commentstr, false, "", ccRemove);
		result = createStatus(env, true, "Checkin Successful");
	}
	catch(_com_error& cerror) 
	{ 
		result = createStatus(env, false, cerror.Description());
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
 * Class:     net_sourceforge_eclipseccase_jni_Clearcase
 * Method:    uncheckout
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jobject JNICALL Java_net_sourceforge_eclipseccase_jni_Clearcase_uncheckout
  (JNIEnv * env, jclass obj, jstring file, jboolean keep)
{
	jobject result = NULL;
	const char *filestr = env->GetStringUTFChars(file, 0);

	try 
	{ 
		ICCVersionPtr ver = ccase->GetVersion(filestr);
		ICCCheckedOutFilePtr cofile = ccase->GetCheckedOutFile(filestr);
		cofile->UnCheckOut(keep ? ccKeep : ccRemove);
		result = createStatus(env, true, "Uncheckout Successful");
	}
	catch(_com_error& cerror) 
	{ 
		result = createStatus(env, false, cerror.Description());
	}
	catch(...)
	{
		raiseJNIException(env, "Unhandled Exception in Clearcase JNI layer");
	}

	env->ReleaseStringUTFChars(file, filestr);
	return result;
}

/*
 * Class:     net_sourceforge_eclipseccase_jni_Clearcase
 * Method:    delete
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jobject JNICALL Java_net_sourceforge_eclipseccase_jni_Clearcase_delete
  (JNIEnv * env, jclass obj, jstring file, jstring comment)
{
	jobject result = NULL;
	const char *filestr = env->GetStringUTFChars(file, 0);
	const char *commentstr = env->GetStringUTFChars(comment, 0);

	try 
	{ 
		ICCVersionPtr ver = ccase->GetVersion(filestr);
		ICCElementPtr elt = ver->GetElement();
		elt->RemoveName(commentstr, true);
		result = createStatus(env, true, "Delete Successful");
	}
	catch(_com_error& cerror) 
	{ 
		result = createStatus(env, false, cerror.Description());
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
 * Class:     net_sourceforge_eclipseccase_jni_Clearcase
 * Method:    add
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jobject JNICALL Java_net_sourceforge_eclipseccase_jni_Clearcase_add
  (JNIEnv * env, jclass obj, jstring file, jstring comment, jboolean isdirectory)
{
	jobject result = NULL;
	const char *filestr = env->GetStringUTFChars(file, 0);
	const char *commentstr = env->GetStringUTFChars(comment, 0);

	try 
	{ 
		ICCCheckedOutFilePtr cofile;
		if (isdirectory)
			cofile = ccase->CreateElement(filestr, commentstr, false, "directory");
		else
			cofile = ccase->CreateElement(filestr, commentstr, false);
		result = createStatus(env, true, "Add Successful");
	}
	catch(_com_error& cerror) 
	{ 
		result = createStatus(env, false, cerror.Description());
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
 * Class:     net_sourceforge_eclipseccase_jni_Clearcase
 * Method:    rename
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Lorg/eclipse/team/clearcase/jni/Clearcase$Status;
 */
JNIEXPORT jobject JNICALL Java_net_sourceforge_eclipseccase_jni_Clearcase_move
  (JNIEnv * env, jclass obj, jstring file, jstring newfile, jstring comment)
{
	jobject result = NULL;
	const char *filestr = env->GetStringUTFChars(file, 0);
	const char *newfilestr = env->GetStringUTFChars(newfile, 0);
	const char *commentstr = env->GetStringUTFChars(comment, 0);

	try 
	{ 
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
		result = createStatus(env, false, cerror.Description());
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
 * Class:     net_sourceforge_eclipseccase_jni_Clearcase
 * Method:    cleartool
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jobject JNICALL Java_net_sourceforge_eclipseccase_jni_Clearcase_cleartool
  (JNIEnv * env, jclass obj, jstring cmd)
{
	jobject result = NULL;
	const char *cmdstr = env->GetStringUTFChars(cmd, 0);
	
	try 
	{
		// Issue a ClearTool command 
		_bstr_t output = cleartool->CmdExec(cmdstr);
		result = createStatus(env, true, (const char*) output);
	}
	catch(_com_error& cerror) 
	{ 
		result = createStatus(env, false, cerror.Description());
	}
	catch(...)
	{
		raiseJNIException(env, "Unhandled Exception in Clearcase JNI layer");
	}

	env->ReleaseStringUTFChars(cmd, cmdstr);
	return result;
}

/*
 * Class:     net_sourceforge_eclipseccase_jni_Clearcase
 * Method:    isCheckedOut
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_net_sourceforge_eclipseccase_jni_Clearcase_isCheckedOut
  (JNIEnv * env, jclass obj, jstring file)
{
	boolean result = false;
	const char *filestr = env->GetStringUTFChars(file, 0);

	try 
	{ 
		ICCVersionPtr ver = ccase->GetVersion(filestr);
		result = ver->GetIsCheckedOut();
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

	env->ReleaseStringUTFChars(file, filestr);
	return result;
}

/*
 * Class:     net_sourceforge_eclipseccase_jni_Clearcase
 * Method:    isElement
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_net_sourceforge_eclipseccase_jni_Clearcase_isElement
  (JNIEnv * env, jclass obj, jstring file)
{
	boolean result = false;
	const char *filestr = env->GetStringUTFChars(file, 0);

	try 
	{ 
		ICCVersionPtr ver = ccase->GetVersion(filestr);
		result = true;
	}
	catch(_com_error& cerror) 
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
 * Class:     net_sourceforge_eclipseccase_jni_Clearcase
 * Method:    isDifferent
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_net_sourceforge_eclipseccase_jni_Clearcase_isDifferent
  (JNIEnv * env, jclass obj, jstring file)
{
	boolean result = false;
	const char *filestr = env->GetStringUTFChars(file, 0);

	try 
	{ 
		ICCVersionPtr ver = ccase->GetVersion(filestr);
		result = ver->GetIsDifferent();
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

	env->ReleaseStringUTFChars(file, filestr);
	return result;
}

#ifdef __cplusplus
}
#endif

