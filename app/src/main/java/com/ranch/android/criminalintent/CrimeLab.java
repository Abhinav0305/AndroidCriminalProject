package com.ranch.android.criminalintent;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ranch.android.criminalintent.database.CrimeBaseHelper;
import com.ranch.android.criminalintent.database.CrimeCursorWrapper;
import com.ranch.android.criminalintent.database.CrimeDbSchema;
import com.ranch.android.criminalintent.database.CrimeDbSchema.CrimeTable;

import java.io.File;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

public class CrimeLab {

    private static CrimeLab sCrimeLab;
    private Context mContext;
    private SQLiteDatabase mDatabase;

    public static CrimeLab get(Context context){
        if(sCrimeLab == null){
            sCrimeLab = new CrimeLab(context);
        }
        return sCrimeLab;
    }

    private CrimeLab(Context context){
        mContext = context.getApplicationContext();
        mDatabase = new CrimeBaseHelper(mContext)
                    .getWritableDatabase();

    }

    public List<Crime> getCrimes(){

        List<Crime> crimes = new ArrayList<>();

        CrimeCursorWrapper cursor = queryCrimes(null,null);
        try{
            cursor.moveToFirst();
            while (!cursor.isAfterLast()){
                crimes.add(cursor.getCrime());
                cursor.moveToNext();
            }
        }finally {
            cursor.close();
        }

        return crimes;
    }

    public Crime getCrime(UUID id){
        CrimeCursorWrapper cursor = queryCrimes(
                CrimeTable.Cols.UUID+" =?",
                new String []{id.toString()}
        );

        try{
            if(cursor.getCount() == 0){
                return null;
            }
            cursor.moveToFirst();
            return cursor.getCrime();
        }finally {
            cursor.close();
        }
    }

    public void add(Crime c){
        ContentValues values = getContentValues(c);
        mDatabase.insert(CrimeTable.NAME,null,values);
    }

    public void updateCrime(Crime crime){
        String uuidString = crime.getmId().toString();
        ContentValues values = getContentValues(crime);

        mDatabase.update(CrimeTable.NAME,values,CrimeTable.Cols.UUID
        +" =?",
                new String[]{uuidString});
    }

    private CrimeCursorWrapper queryCrimes(String whereClause,String [] whereArgs){
        Cursor cursor = mDatabase.query(
            CrimeTable.NAME,
            null,
            whereClause,
            whereArgs,
            null,
            null,
                null);

        return new CrimeCursorWrapper(cursor);
    }


    private static ContentValues getContentValues(Crime crime){
        ContentValues values = new ContentValues();
        values.put(CrimeTable.Cols.UUID,crime.getmId().toString());
        values.put(CrimeTable.Cols.TITLE,crime.getmTitle());
        values.put(CrimeTable.Cols.DATE,crime.getmDate().getTime());
        values.put(CrimeTable.Cols.SOLVED,crime.ismSolved());
        values.put(CrimeTable.Cols.SUSPECT,crime.getmSuspect());
        return values;
    }

    public File getPhotoFile(Crime crime){
        File fileDir = mContext.getFilesDir();
        return new File(fileDir,crime.getPhotoFilename());
    }
}
