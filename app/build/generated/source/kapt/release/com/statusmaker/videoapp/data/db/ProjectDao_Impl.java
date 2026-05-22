package com.statusmaker.videoapp.data.db;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.statusmaker.videoapp.data.model.Project;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ProjectDao_Impl implements ProjectDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Project> __insertionAdapterOfProject;

  private final EntityDeletionOrUpdateAdapter<Project> __deletionAdapterOfProject;

  private final EntityDeletionOrUpdateAdapter<Project> __updateAdapterOfProject;

  private final SharedSQLiteStatement __preparedStmtOfDeleteById;

  public ProjectDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfProject = new EntityInsertionAdapter<Project>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `projects` (`id`,`templateId`,`personName`,`villageName`,`festivalName`,`personPhotoUri`,`outputVideoPath`,`createdAt`,`isShared`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Project entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getTemplateId() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getTemplateId());
        }
        if (entity.getPersonName() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getPersonName());
        }
        if (entity.getVillageName() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getVillageName());
        }
        if (entity.getFestivalName() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getFestivalName());
        }
        if (entity.getPersonPhotoUri() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getPersonPhotoUri());
        }
        if (entity.getOutputVideoPath() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getOutputVideoPath());
        }
        statement.bindLong(8, entity.getCreatedAt());
        final int _tmp = entity.isShared() ? 1 : 0;
        statement.bindLong(9, _tmp);
      }
    };
    this.__deletionAdapterOfProject = new EntityDeletionOrUpdateAdapter<Project>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `projects` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Project entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfProject = new EntityDeletionOrUpdateAdapter<Project>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `projects` SET `id` = ?,`templateId` = ?,`personName` = ?,`villageName` = ?,`festivalName` = ?,`personPhotoUri` = ?,`outputVideoPath` = ?,`createdAt` = ?,`isShared` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Project entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getTemplateId() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getTemplateId());
        }
        if (entity.getPersonName() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getPersonName());
        }
        if (entity.getVillageName() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getVillageName());
        }
        if (entity.getFestivalName() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getFestivalName());
        }
        if (entity.getPersonPhotoUri() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getPersonPhotoUri());
        }
        if (entity.getOutputVideoPath() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getOutputVideoPath());
        }
        statement.bindLong(8, entity.getCreatedAt());
        final int _tmp = entity.isShared() ? 1 : 0;
        statement.bindLong(9, _tmp);
        statement.bindLong(10, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM projects WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final Project project, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfProject.insertAndReturnId(project);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final Project project, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfProject.handle(project);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final Project project, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfProject.handle(project);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteById(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteById.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Project>> getAllProjects() {
    final String _sql = "SELECT * FROM projects ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"projects"}, new Callable<List<Project>>() {
      @Override
      @NonNull
      public List<Project> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTemplateId = CursorUtil.getColumnIndexOrThrow(_cursor, "templateId");
          final int _cursorIndexOfPersonName = CursorUtil.getColumnIndexOrThrow(_cursor, "personName");
          final int _cursorIndexOfVillageName = CursorUtil.getColumnIndexOrThrow(_cursor, "villageName");
          final int _cursorIndexOfFestivalName = CursorUtil.getColumnIndexOrThrow(_cursor, "festivalName");
          final int _cursorIndexOfPersonPhotoUri = CursorUtil.getColumnIndexOrThrow(_cursor, "personPhotoUri");
          final int _cursorIndexOfOutputVideoPath = CursorUtil.getColumnIndexOrThrow(_cursor, "outputVideoPath");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfIsShared = CursorUtil.getColumnIndexOrThrow(_cursor, "isShared");
          final List<Project> _result = new ArrayList<Project>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Project _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTemplateId;
            if (_cursor.isNull(_cursorIndexOfTemplateId)) {
              _tmpTemplateId = null;
            } else {
              _tmpTemplateId = _cursor.getString(_cursorIndexOfTemplateId);
            }
            final String _tmpPersonName;
            if (_cursor.isNull(_cursorIndexOfPersonName)) {
              _tmpPersonName = null;
            } else {
              _tmpPersonName = _cursor.getString(_cursorIndexOfPersonName);
            }
            final String _tmpVillageName;
            if (_cursor.isNull(_cursorIndexOfVillageName)) {
              _tmpVillageName = null;
            } else {
              _tmpVillageName = _cursor.getString(_cursorIndexOfVillageName);
            }
            final String _tmpFestivalName;
            if (_cursor.isNull(_cursorIndexOfFestivalName)) {
              _tmpFestivalName = null;
            } else {
              _tmpFestivalName = _cursor.getString(_cursorIndexOfFestivalName);
            }
            final String _tmpPersonPhotoUri;
            if (_cursor.isNull(_cursorIndexOfPersonPhotoUri)) {
              _tmpPersonPhotoUri = null;
            } else {
              _tmpPersonPhotoUri = _cursor.getString(_cursorIndexOfPersonPhotoUri);
            }
            final String _tmpOutputVideoPath;
            if (_cursor.isNull(_cursorIndexOfOutputVideoPath)) {
              _tmpOutputVideoPath = null;
            } else {
              _tmpOutputVideoPath = _cursor.getString(_cursorIndexOfOutputVideoPath);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final boolean _tmpIsShared;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsShared);
            _tmpIsShared = _tmp != 0;
            _item = new Project(_tmpId,_tmpTemplateId,_tmpPersonName,_tmpVillageName,_tmpFestivalName,_tmpPersonPhotoUri,_tmpOutputVideoPath,_tmpCreatedAt,_tmpIsShared);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getById(final long id, final Continuation<? super Project> $completion) {
    final String _sql = "SELECT * FROM projects WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Project>() {
      @Override
      @Nullable
      public Project call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTemplateId = CursorUtil.getColumnIndexOrThrow(_cursor, "templateId");
          final int _cursorIndexOfPersonName = CursorUtil.getColumnIndexOrThrow(_cursor, "personName");
          final int _cursorIndexOfVillageName = CursorUtil.getColumnIndexOrThrow(_cursor, "villageName");
          final int _cursorIndexOfFestivalName = CursorUtil.getColumnIndexOrThrow(_cursor, "festivalName");
          final int _cursorIndexOfPersonPhotoUri = CursorUtil.getColumnIndexOrThrow(_cursor, "personPhotoUri");
          final int _cursorIndexOfOutputVideoPath = CursorUtil.getColumnIndexOrThrow(_cursor, "outputVideoPath");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfIsShared = CursorUtil.getColumnIndexOrThrow(_cursor, "isShared");
          final Project _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTemplateId;
            if (_cursor.isNull(_cursorIndexOfTemplateId)) {
              _tmpTemplateId = null;
            } else {
              _tmpTemplateId = _cursor.getString(_cursorIndexOfTemplateId);
            }
            final String _tmpPersonName;
            if (_cursor.isNull(_cursorIndexOfPersonName)) {
              _tmpPersonName = null;
            } else {
              _tmpPersonName = _cursor.getString(_cursorIndexOfPersonName);
            }
            final String _tmpVillageName;
            if (_cursor.isNull(_cursorIndexOfVillageName)) {
              _tmpVillageName = null;
            } else {
              _tmpVillageName = _cursor.getString(_cursorIndexOfVillageName);
            }
            final String _tmpFestivalName;
            if (_cursor.isNull(_cursorIndexOfFestivalName)) {
              _tmpFestivalName = null;
            } else {
              _tmpFestivalName = _cursor.getString(_cursorIndexOfFestivalName);
            }
            final String _tmpPersonPhotoUri;
            if (_cursor.isNull(_cursorIndexOfPersonPhotoUri)) {
              _tmpPersonPhotoUri = null;
            } else {
              _tmpPersonPhotoUri = _cursor.getString(_cursorIndexOfPersonPhotoUri);
            }
            final String _tmpOutputVideoPath;
            if (_cursor.isNull(_cursorIndexOfOutputVideoPath)) {
              _tmpOutputVideoPath = null;
            } else {
              _tmpOutputVideoPath = _cursor.getString(_cursorIndexOfOutputVideoPath);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final boolean _tmpIsShared;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsShared);
            _tmpIsShared = _tmp != 0;
            _result = new Project(_tmpId,_tmpTemplateId,_tmpPersonName,_tmpVillageName,_tmpFestivalName,_tmpPersonPhotoUri,_tmpOutputVideoPath,_tmpCreatedAt,_tmpIsShared);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getProjectCount(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM projects";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final Integer _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getInt(0);
            }
            _result = _tmp;
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
