package org.itoapp.strict.database;

import android.content.Context;
import android.util.Log;

import org.itoapp.strict.database.entities.LastReport;
import org.itoapp.strict.database.entities.SeenTCN;
import org.itoapp.strict.network.NetworkHelper;

import java.util.Date;

import static org.itoapp.strict.Constants.HASH_LENGTH;
import static org.itoapp.strict.Helper.byte2Hex;

public class ItoDBHelper {

    private final Context context;

    public ItoDBHelper(Context context) {
        this.context = context;
    }

    private static final String LOG_TAG = "ITODBHelper";

    private void checkHashedUUID(byte[] hashedUUID) {

        if (hashedUUID == null || hashedUUID.length != HASH_LENGTH)
            throw new IllegalArgumentException();
    }


    public void insertContact(byte[] hashed_uuid, int proximity, long duration) {
        checkHashedUUID(hashed_uuid);
        String tcn64 = byte2Hex(hashed_uuid);

        RoomDB db = RoomDB.getInstance(context);

        SeenTCN seenTCN = db.seenTCNDao().findSeenTCNByHash(tcn64);
        if (seenTCN == null) {
            seenTCN = new SeenTCN(tcn64, new Date(), proximity, duration);
            db.seenTCNDao().insert(seenTCN);
        } else {
            seenTCN.lastSeen = new Date();
            seenTCN.proximity = (seenTCN.proximity + proximity) / 2;
            seenTCN.duration += duration;
            db.seenTCNDao().update(seenTCN);
        }
        Log.d(LOG_TAG, "Inserted contact: " + seenTCN);

    }


    public int getLatestFetchTime() {
        Log.d(LOG_TAG, "Getting latest fetch time");
        RoomDB db = RoomDB.getInstance(context);
        final LastReport lastReportHashForServer = db.lastReportDao().getLastReportHashForServer(NetworkHelper.BASE_URL);
        if (lastReportHashForServer == null)
            return 0;
        return (int) lastReportHashForServer.lastcheck.getTime()/1000; // FIXME!
    }

}
