package durdinapps.rxfirebase2;


import android.support.annotation.NonNull;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;

public abstract class DocumentSnapshotMapper<T, U> implements Function<T, U> {

   private DocumentSnapshotMapper() {
   }

   public static <U> DocumentSnapshotMapper<DocumentSnapshot, U> of(Class<U> clazz) {
      return new TypedDocumentSnapshotMapper<U>(clazz);
   }

   public static <U> DocumentSnapshotMapper<QuerySnapshot, List<U>> listOf(Class<U> clazz) {
      return new DocumentSnapshotMapper.TypedListDocumentSnapshotMapper<>(clazz);
   }

   public static <U> DocumentSnapshotMapper<QuerySnapshot, List<U>> listOf(Class<U> clazz, Function<DocumentSnapshot, U> mapper) {
      return new DocumentSnapshotMapper.TypedListDocumentSnapshotMapper<>(clazz, mapper);
   }

   public static <U> TypedMapDocumentSnapshotMapper<U> mapOf(Class<U> clazz) {
      return new DocumentSnapshotMapper.TypedMapDocumentSnapshotMapper<>(clazz);
   }

   private static <U> U getDataSnapshotTypedValue(DocumentSnapshot documentSnapshot, Class<U> clazz) {
      return documentSnapshot.toObject(clazz);
   }

   private static class TypedDocumentSnapshotMapper<U> extends DocumentSnapshotMapper<DocumentSnapshot, U> {

      private final Class<U> clazz;

      public TypedDocumentSnapshotMapper(final Class<U> clazz) {
         this.clazz = clazz;
      }

      @Override
      public U apply(final DocumentSnapshot documentSnapshot) {
         return getDataSnapshotTypedValue(documentSnapshot, clazz);
      }
   }

   private static class TypedListDocumentSnapshotMapper<U> extends DocumentSnapshotMapper<QuerySnapshot, List<U>> {

      private final Class<U> clazz;
      private final Function<DocumentSnapshot, U> mapper;

      TypedListDocumentSnapshotMapper(final Class<U> clazz) {
         this(clazz, null);
      }

      TypedListDocumentSnapshotMapper(final Class<U> clazz, Function<DocumentSnapshot, U> mapper) {
         this.clazz = clazz;
         this.mapper = mapper;
      }

      @Override
      public List<U> apply(final QuerySnapshot querySnapshot) throws Exception {
         List<U> items = new ArrayList<>();
         for (DocumentSnapshot documentSnapshot : querySnapshot) {
            items.add(mapper != null
               ? mapper.apply(documentSnapshot)
               : getDataSnapshotTypedValue(documentSnapshot, clazz));
         }
         return items;
      }
   }


   private static class TypedMapDocumentSnapshotMapper<U> extends DocumentSnapshotMapper<QuerySnapshot, LinkedHashMap<String, U>> {

      private final Class<U> clazz;

      TypedMapDocumentSnapshotMapper(final Class<U> clazz) {
         this.clazz = clazz;
      }

      @Override
      public LinkedHashMap<String, U> apply(final QuerySnapshot querySnapshot) {
         LinkedHashMap<String, U> items = new LinkedHashMap<>();
         for (DocumentSnapshot documentSnapshot : querySnapshot) {
            items.put(documentSnapshot.getId(), getDataSnapshotTypedValue(documentSnapshot, clazz));
         }
         return items;
      }
   }

   static final Predicate<QuerySnapshot> QUERY_EXISTENCE_PREDICATE = new Predicate<QuerySnapshot>() {
      @Override
      public boolean test(@NonNull QuerySnapshot querySnapshot) throws Exception {
         return querySnapshot.isEmpty();
      }
   };

   static final Predicate<DocumentSnapshot> DOCUMENT_EXISTENCE_PREDICATE = new Predicate<DocumentSnapshot>() {
      @Override
      public boolean test(@NonNull DocumentSnapshot documentSnapshot) throws Exception {
         return documentSnapshot.exists();
      }
   };
}