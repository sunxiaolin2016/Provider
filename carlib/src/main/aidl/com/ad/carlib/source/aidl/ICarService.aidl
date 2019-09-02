// ICarService.aidl
package com.ad.carlib.source.aidl;

// Declare any non-default types here with import statements
import com.ad.carlib.source.aidl.ICarClient;

interface ICarService {
    int createSource(int sourceID,ICarClient client);
    void destroySource(int sourceID,int bindID);

    Bundle sourceAction(boolean bDirectCall,int source,int action,in Bundle b);
}
