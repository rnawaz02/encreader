package com.sovworks.eds.fs.util;

import java.io.IOException;
import java.io.Serializable;

//import android.os.Parcelable;

public interface SrcDstCollection extends Iterable<SrcDstCollection.SrcDst>, Serializable
{
	interface SrcDst
	{
		//com.sovworks.eds.locations.Location getSrcLocation() throws IOException;
		//com.sovworks.eds.locations.Location getDstLocation() throws IOException;
	}

}
