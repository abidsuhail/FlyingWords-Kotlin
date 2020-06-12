const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

const db = admin.firestore();


//-----------------------NEW POST CREATED--------------------------------------------------------->

exports.createPost = functions.firestore
    .document('organized_user_posts/{post_owner_id}/my_posts/{post_id}')
    .onCreate((snap, context) => {
      // Get an object representing the document
      // e.g. {'name': 'Marie', 'age': 66}
      const post = snap.data();
      const postOwnerId = post.byUid;
      const ownerfriendRef = db.collection('friends').doc('friends').collection(postOwnerId);
      const batch = db.batch();

	ownerfriendRef.get().then(list=>{

		list.forEach(item=>{
			
				 const ref = db.collection('feeds').doc(item.data().uid).collection('feeds').doc(post.postId);
		         batch.set(ref,post);
	           
		});
	
	return batch.commit();

	}).catch(err=>{console.log(err);});


 });

//-----------------------NEW STORY CREATED/ADDED--------------------------------------------------------->
exports.storyAdded = functions.firestore
    .document('all_stories/{user_id}')
    .onCreate((snap, context) => {


      const story = snap.data();
      const storyOwnerId = story.byUid;
      const ownerfriendRef = db.collection('friends').doc('friends').collection(storyOwnerId);
      const batch = db.batch();

	ownerfriendRef.get().then(ownerFriendlist=>{

		ownerFriendlist.forEach(friend=>{
			
				 const ref = db.collection('stories').doc('users').collection(friend.data().uid).doc(story.byUid);
		         batch.set(ref,story);
	           
		});
	
	return batch.commit();

	}).catch(err=>{console.log(err);});


 });

//-----------------------STORY UPDATED--------------------------------------------------------->

exports.storyUpdated = functions.firestore
    .document('all_stories/{user_id}')
    .onUpdate((snap, context) => {

      const story = snap.after.data();
      const storyOwnerId = story.byUid;
      const ownerfriendRef = db.collection('friends').doc('friends').collection(storyOwnerId);
      const batch = db.batch();

		        ownerfriendRef.get().then(ownerFriendlist=>{

				ownerFriendlist.forEach(friend=>{
					
						 const ref = db.collection('stories').doc('users').collection(friend.data().uid).doc(story.byUid);
				         batch.set(ref,story);
			           
				});
			
			return batch.commit();

			}).catch(err=>{console.log(err);});
	
 });

 //-----------------------STORY DELETED--------------------------------------------------------->

exports.storyDeleted = functions.firestore
    .document('all_stories/{user_id}')
    .onDelete((snap, context) => {

      const story = snap.data();
      const storyOwnerId = story.byUid;
      const ownerfriendRef = db.collection('friends').doc('friends').collection(storyOwnerId);
      const batch = db.batch();

	ownerfriendRef.get().then(ownerFriendlist=>{

		ownerFriendlist.forEach(friend=>{
			
				 const ref = db.collection('stories').doc('users').collection(friend.data().uid).doc(story.byUid);
		         batch.delete(ref);
	           
		});
	
	return batch.commit();

	}).catch(err=>{console.log(err);});


 });
//<---------------------------------------------------on post update----------------------------------------------------->
 exports.updatePost = functions.firestore
    .document('organized_user_posts/{post_owner_id}/my_posts/{post_id}')
    .onUpdate((snap, context) => {
      // Get an object representing the document
      // e.g. {'name': 'Marie', 'age': 66}
      const post = snap.after.data();
      const postOwnerId = post.byUid;
      const ownerfriendRef = db.collection('friends').doc('friends').collection(postOwnerId);
      const batch = db.batch();

      const myFeedsRef = db.collection('feeds').doc(postOwnerId).collection('feeds').doc(post.postId);
      batch.update(myFeedsRef,post);

	ownerfriendRef.get().then(list=>{
		list.forEach(item=>{
			 const ref = db.collection('feeds').doc(item.data().uid).collection('feeds').doc(post.postId);
	         batch.update(ref,post);
		});
	
	return batch.commit();

	}).catch(err=>{console.log(err);});

 });

//<---------------------------------------------------on delete post----------------------------------------------------->

exports.deletePost = functions.firestore
    .document('organized_user_posts/{post_owner_id}/my_posts/{post_id}')
    .onDelete((snap, context) => {
      // Get an object representing the document
      // e.g. {'name': 'Marie', 'age': 66}
      const post = snap.data();
      const postOwnerId = post.byUid;
      const ownerfriendRef = db.collection('friends').doc('friends').collection(postOwnerId);
      const batch = db.batch();



	ownerfriendRef.get().then(list=>{
		list.forEach(item=>{
			 const ref = db.collection('feeds').doc(item.data().uid).collection('feeds').doc(post.postId);
	         batch.delete(ref);
		});
	
	return batch.commit();

	}).catch(err=>{console.log(err);});

 });

 //<-------------------------------------FRIEND DELETED/UNFRIEND -------------------------------------------------->

 exports.deletedFriend = functions.firestore
    .document('friends/friends/{user_id}/{user_friend_id}')
    .onDelete((snap, context) => {
      // Get an object representing the document
      // e.g. {'name': 'Marie', 'age': 66}
      const userId = context.params.user_id;
      const deletedUserFriendId = context.params.user_friend_id;

      const userFeedsRef = db.collection('feeds').doc(userId).collection('feeds');
      const userStoriesRef = db.collection('stories').doc('users').collection(userId);
      const batch = db.batch();

	   userFeedsRef.get().then(feedsList=>{
		feedsList.forEach(post=>{
			if (post.data().byUid === deletedUserFriendId) 
			 {
	           batch.delete(post.ref);
	         }
		});
		batch.delete(userStoriesRef.doc(deletedUserFriendId));
	
	return batch.commit();

	}).catch(err=>{console.log(err);});

 });

 // <-------------------------------------------------- FRIEND ADDED ------------------------------------------------->

 exports.addedFriend = functions.firestore
    .document('friends/friends/{user_id}/{user_friend_id}')
    .onCreate((snap, context) => {
      // Get an object representing the document
      // e.g. {'name': 'Marie', 'age': 66}
      const userId = context.params.user_id;
      const addedUserFriendId = context.params.user_friend_id;

      const organizedPostRef = db.collection('organized_user_posts').doc(userId).collection('my_posts');
      const userStoriesRef = db.collection('all_stories').doc(userId);
      const addedFriendStoriesRef = db.collection('stories').doc('users').collection(addedUserFriendId).doc(userId);
      const addedUserFeedsRef = db.collection('feeds').doc(addedUserFriendId).collection('feeds');
      const batch = db.batch();

	   organizedPostRef.get().then(feedsList=>{

	   	feedsList.forEach(post=>{
	   
	   	 batch.set(addedUserFeedsRef.doc(post.data().postId),post.data());

		});

	   	return null;

      }).then(()=>{

      	return userStoriesRef.get();

      }).then(myStory=>{

      	if (myStory.exists) {
		batch.set(addedFriendStoriesRef,myStory.data());
	    }

		return null;

      }).then(()=>{

      	return batch.commit();

      }).catch(err=>{console.log(err);});
 	

});