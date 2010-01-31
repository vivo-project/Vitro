/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vedit.beans;

/**
 * User Session information
 * @author John Fereira
 * @since 29.06.2004
 */
public class UserSession {
   private int currentEntityId;
   private int currentPortalId;
   private int currentTabId;
   private String lastSearchURL;
   private boolean isPatronAuthenticated;
   private boolean isAdminAuthenticated;
   private String referer;
   private String lastSearchTerm;

   private String flag1Pref;
   public  void setFlag1Pref(String s) { flag1Pref=s; }
   public  String getFlag1Pref()  { return flag1Pref; }

   private String flag2Pref;
   public  void setFlag2Pref(String s) { flag2Pref=s; }
   public  String getFlag2Pref()  { return flag2Pref; }

   private String flag3Pref;
   public  void setFlag3Pref(String s) { flag3Pref=s; }
   public  String getFlag3Pref()  { return flag3Pref; }

//search wrapper was part of the mysql full text search, no longer in use.
//   private SearchWrapper searchWrapper;
//   public  void          setSearchWrapper(SearchWrapper sw) { searchWrapper=sw; }
//   public  SearchWrapper getSearchWrapper()    { return searchWrapper; }
//   public  void          disposeOf(SearchWrapper sw) {
//     if (sw!=null){
//         sw.dispose();
//     }
//     this.searchWrapper=null;
//   }

   /** constructor */
   public UserSession() {
      this.isPatronAuthenticated = false;
      this.isAdminAuthenticated = false;
   }

   /**
    * set current entity id
    * @param currentEntityId : unique id
    */
   public void setCurrentEntityId(int currentEntityId) {
      this.currentEntityId = currentEntityId;
   }

   /**
    * get Current entity Id
    * @return : the entity Id
    */
   public int getCurrentEntityId() {
      return currentEntityId;
   }

   /**
    * set current portal id
    * @param currentPortalId : unique id
    */
   public void setCurrentPortalId(int currentPortalId) {
      this.currentPortalId = currentPortalId;
   }

   /**
    * get Current portal Id
    * @return : the portal Id
    */
   public int getCurrentPortalId() {
      return currentPortalId;
   }

   /**
    * set current tab id
    * @param currentTabId : unique id
    */
   public void setCurrentTabId(int currentTabId) {
      this.currentTabId = currentTabId;
   }

   /**
    * get current tab id
    * @return : the tab Id
    */
   public int getCurrentTabId() {
      return currentTabId;
   }


   /**
    * set last SearchURL in session
    * @param lastSearchURL : a url string
    */
   public void setLastSearchURL(String lastSearchURL) {
      this.lastSearchURL = lastSearchURL;
   }

   /**
    * get last Search URL
    * @return : last search url string
    */
   public String getLastSearchURL() {
      return lastSearchURL;
   }

   /**
    * Set boolen flag to indicated if patron has authenticated
    * @param isPatronAuthenticated : true or false
    */
   public void setIsPatronAuthenticated(boolean isPatronAuthenticated) {
      this.isPatronAuthenticated = isPatronAuthenticated;
   }

   /**
    * get boolean flag indicating whethor or not patron has authenticated
    * @return : true or false
    */
   public boolean getIsPatronAuthenticated() {
      return isPatronAuthenticated;
   }

   /**
    * set boolean flag indicating whether or not an Administrator has
    * authenticated
    * @param isAdminAuthenticated : true or false
    */
   public void setIsAdminAuthenticated(boolean isAdminAuthenticated) {
      this.isAdminAuthenticated = isAdminAuthenticated;
   }

   /**
    * get boolean flag indicating whether or not an Administrator has
    * authenticated
    * @return : true or false
    */
   public boolean getIsAdminAuthenticated() {
      return isAdminAuthenticated;
   }

   /**
    * set referer url in session
    * @param referer : a referer url string
    */
   public void setReferer(String referer) {
      this.referer = referer;
   }

   /**
    * get referer url in session
    * @return : a referer url string
    */
   public String getReferer() {
      return referer;
   }

   /**
    * set lastSearchTerm in session
    * @param lastSearchTerm :  a lastSearchTerm string
    */
   public void setLastSearchTerm(String lastSearchTerm) {
      this.lastSearchTerm = lastSearchTerm;
   }

   /**
    * get lastSearchTerm in session
    * @return : a lastSearchTerm string
    */
   public String getLastSearchTerm() {
      return lastSearchTerm;
   }


   /**
    * @param isAdminAuthenticated The isAdminAuthenticated to set.
    */
   public void setAdminAuthenticated(boolean isAdminAuthenticated) {
      this.isAdminAuthenticated = isAdminAuthenticated;
   }

   /**
    * @param isPatronAuthenticated The isPatronAuthenticated to set.
    */
   public void setPatronAuthenticated(boolean isPatronAuthenticated) {
      this.isPatronAuthenticated = isPatronAuthenticated;
   }
}
