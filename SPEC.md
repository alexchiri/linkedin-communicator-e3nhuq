# LinkedIn Communicator - Functional Specification

## Overview

LinkedIn Communicator is a multilingual content creation tool designed to help users write, edit, and translate LinkedIn posts across three languages: Swedish, English, and Romanian. The app uses AI assistance (Claude API) to proofread, condense, translate, and optimize content for professional social media posting.

**Target Platform:** Mobile (iOS, Android)
**Primary Use Case:** Creating multilingual LinkedIn posts with AI assistance
**Supported Languages:** Swedish, English, Romanian

---

## Core Concepts

### Posts

A "post" is the primary unit of work in the app. Each post contains:
- Text content in three languages (Swedish, English, Romanian)
- A workflow stage indicating progress
- Creation and modification timestamps
- Version history (up to 50 saved versions)

Users can work on multiple posts simultaneously, switching between them as needed.

### Workflow Stages

Posts progress through six stages:

1. **Draft** - Initial writing stage
2. **Proofread** - After Swedish text has been reviewed for grammar/style
3. **Condensed** - After text has been made more concise
4. **Translated** - After translation to other languages
5. **Reviewed** - After final review of translations
6. **Ready to Post** - Finalized and ready to publish

The workflow stage is indicated by a colored badge (yellow → orange → blue → green → teal → purple).

---

## User Interface

### Main Screen

The main screen shows:
- **Navigation bar** with:
  - App title
  - Cloud sync status indicator
  - Settings button (gear icon)

- **Post management buttons**:
  - Create New Post
  - Load Draft (shows list of saved posts)
  - Save Current Post

- **Content area**:
  - When no post is open: welcome message and instructions
  - When post is open: tabbed editor interface

### Post Editor

When a post is open, the editor displays:

**Top section:**
- Three language tabs (Swedish, English, Romanian)
- Each tab shows a text editor for that language
- Character count for current language (with warnings at 2700/3000 characters)
- Workflow stage badge

**Language Editor (per tab):**
- Large text input area
- Placeholder text: "Write your post in [language]..."
- Character counter showing current/3000 limit
- Visual warning when approaching/exceeding limit (3000 character LinkedIn limit)

**Bottom toolbar** with actions:
- **AI Actions** button - opens menu with AI-powered features
- **Post Switcher** - switch between open posts
- **Version History** - view and restore previous versions
- **Close Post** - save and close current post

### AI Actions Menu

When user taps "AI Actions", a modal sheet appears with:

**For Swedish tab:**
- Proofread Swedish Text
- Make Text Concise
- Translate to English
- Translate to Romanian
- Translate to Both Languages
- Get Translation Help

**For English tab:**
- Translate to Swedish
- Get Translation Help

**For Romanian tab:**
- Translate to Swedish
- Get Translation Help

Each action shows a loading indicator while processing and displays errors if they occur.

### Translation Help Feature

When user requests translation help for a selected text:
1. User selects/highlights text in the editor
2. Taps "Get Translation Help"
3. A modal appears showing:
   - Original text
   - Alternative translations/phrasings
   - Option to accept a suggestion (replaces original text)
   - Option to dismiss

### Diff View (Change Comparison)

After AI operations that modify text, users see a side-by-side comparison:
- **Left side:** Original text
- **Right side:** AI-modified text
- **Actions:**
  - Accept Changes (replaces original with modified)
  - Reject Changes (keeps original)
  - Toggle between side-by-side and unified view

### Version History

Displays a chronological list of saved versions:
- Timestamp of each version
- Workflow stage at that time
- Preview of Swedish text (first 100 characters)
- Tap to view full version
- Option to restore any version (creates new current version)

### Draft Picker

Shows all saved posts:
- List sorted by last modified date (newest first)
- Each item shows:
  - Swedish text preview (first 200 characters)
  - Workflow stage badge
  - Last modified date
  - Tap to open
  - Swipe to delete

### Post Switcher

Shows all currently open posts (in memory):
- Similar layout to draft picker
- Tap to switch to that post
- Shows active post with highlight
- Quick navigation between multiple posts

### Settings Screen

**API Configuration:**
- Claude API Key input field (secure, hidden text)
- Save/Update API Key button
- Status indicator (configured/not configured)

**Content Preferences:**
- Markdown Mode toggle switch
  - OFF (default): AI returns plain text
  - ON: AI can use/preserve markdown formatting (for blog posts)

**Cloud Sync:**
- iCloud Sync enable/disable toggle
- Sync status display (idle/syncing/synced/error/offline)
- "Sync Now" manual sync button
- Last sync timestamp

**App Information:**
- App version
- Build number

---

## Detailed Feature Descriptions

### 1. Creating and Managing Posts

**Creating a New Post:**
1. User taps "Create New Post"
2. App creates empty post with workflow stage "Draft"
3. Editor opens with Swedish tab active
4. User can start typing immediately

**Saving Posts:**
- Auto-save: Automatically saves every 30 seconds if post has content
- Manual save: Via "Save" button in post management area
- On close: Saves when closing a post
- Saved to device storage as JSON files

**Loading Drafts:**
1. User taps "Load Draft"
2. List of all saved posts appears
3. User taps a post to open it
4. Post loads into editor

**Deleting Drafts:**
- Swipe left on post in draft picker
- Confirm deletion
- Post permanently removed

### 2. Text Editing

**Basic Editing:**
- Type directly in text area for each language
- Copy/paste support
- Undo/redo (platform standard)
- Text selection for translation help

**Character Limits:**
- LinkedIn limit: 3000 characters
- Warning at 2700 characters (yellow indicator)
- Error at 3000+ characters (red indicator)
- Count includes all characters (letters, spaces, punctuation)

### 3. AI-Powered Features

All AI operations require a configured Claude API key (set in Settings).

#### Proofread Swedish Text

**Purpose:** Check grammar, spelling, style, and professional tone
**Input:** Current Swedish text
**Output:** Corrected/improved Swedish text
**Behavior:**
- Creates version snapshot before processing
- Shows diff view with changes
- Updates workflow stage to "Proofread" if accepted
- Returns suggestions for improvements

#### Make Text Concise

**Purpose:** Reduce length while preserving meaning
**Input:** Current Swedish text
**Output:** Shortened Swedish text
**Behavior:**
- Creates version snapshot
- Shows diff view
- Updates workflow stage to "Condensed" if accepted
- Aims to reduce character count for LinkedIn's limit

#### Translate to English

**Purpose:** Translate Swedish post to English
**Input:** Current Swedish text
**Output:** English translation
**Behavior:**
- Creates version snapshot
- Translates and populates English tab
- Updates workflow stage to "Translated" if accepted
- Automatically switches to English tab to show result

#### Translate to Romanian

**Purpose:** Translate Swedish post to Romanian
**Input:** Current Swedish text
**Output:** Romanian translation
**Behavior:**
- Creates version snapshot
- Translates and populates Romanian tab
- Updates workflow stage to "Translated" if accepted
- Automatically switches to Romanian tab to show result

#### Translate to Both Languages

**Purpose:** Translate Swedish to both English and Romanian simultaneously
**Input:** Current Swedish text
**Output:** Both English and Romanian translations
**Behavior:**
- Creates version snapshot
- Translates to both languages in parallel
- Shows success message when complete
- Updates workflow stage to "Translated" if accepted
- User can switch tabs to view results

#### Back-translate (English/Romanian to Swedish)

**Purpose:** Translate English or Romanian back to Swedish
**Input:** Current English or Romanian text
**Output:** Swedish translation
**Behavior:**
- Creates version snapshot
- Translates to Swedish
- Populates Swedish tab
- Useful for starting with English/Romanian content

#### Get Translation Help

**Purpose:** Get alternative phrasings for selected text
**Input:** Selected text from any language
**Output:** 3-5 alternative translations/phrasings
**Behavior:**
- User selects text in editor
- Taps "Get Translation Help"
- Modal shows alternatives
- User can tap to replace original with chosen alternative
- Does not create version snapshot (minor change)
- Does not change workflow stage

### 4. Version History

**Automatic Versioning:**
- Before each AI operation, current state is saved
- Timestamp recorded
- Maximum 50 versions per post
- Oldest versions automatically deleted when limit exceeded

**Viewing History:**
1. User taps "Version History" in bottom toolbar
2. List appears with all saved versions
3. Each shows timestamp, stage, and preview
4. User taps version to see full content

**Restoring Versions:**
1. From version history, user taps version
2. Views full content of that version
3. Taps "Restore"
4. Current post content replaced with selected version
5. New version created with current timestamp
6. Editor updates immediately

### 5. Cross-Device Sync (iCloud/Cloud)

**Purpose:** Keep posts synchronized across user's devices
**Enabled by default:** Yes (can be disabled in Settings)

**Sync Behavior:**
- Initial sync on app launch (if enabled)
- Periodic automatic sync every 5 minutes
- Sync on save/close post
- Receives notifications when changes occur on other devices

**Conflict Resolution:**
- If same post modified on multiple devices
- Version with latest timestamp wins
- User always sees most recent content
- No manual conflict resolution needed

**Sync Status:**
- **Idle:** Cloud icon, ready to sync
- **Syncing:** Circular arrow icon, sync in progress
- **Synced:** Checkmark + cloud icon, successfully synced
- **Error:** Exclamation + cloud icon, sync failed
- **Offline:** Cloud with slash, no network/cloud unavailable

**Manual Sync:**
- User can tap "Sync Now" in Settings
- Forces immediate sync
- Useful after network interruption

**Requirements:**
- User must be signed into iCloud (iOS) or Google account (Android)
- Internet connection required
- Sync can be disabled in Settings if user prefers local-only

### 6. Markdown Mode

**Purpose:** Choose between plain text and markdown-formatted output
**Location:** Settings screen
**Default:** OFF (plain text)

**When OFF:**
- AI strips markdown formatting from responses
- Output is plain text only
- Best for LinkedIn posts (which don't support markdown)

**When ON:**
- AI can add/preserve markdown formatting
- Useful for blog posts or content that will be converted
- Headings, lists, bold, italic, etc. preserved

**Applies to:**
- All AI operations that generate or modify text
- Does not affect already-saved content

---

## User Workflows

### Typical Workflow 1: Create Swedish Post, Translate to All

1. User opens app
2. Taps "Create New Post"
3. Types Swedish content in Swedish tab
4. Taps "AI Actions" → "Proofread Swedish Text"
5. Reviews suggested changes in diff view
6. Accepts changes
7. Taps "AI Actions" → "Make Text Concise" (if needed)
8. Reviews and accepts
9. Taps "AI Actions" → "Translate to Both Languages"
10. Waits for translation to complete
11. Reviews English tab
12. Reviews Romanian tab
13. Makes manual adjustments if needed
14. Post automatically saves
15. When ready, copies content to LinkedIn (outside app)

### Typical Workflow 2: Start with English, Translate to Swedish, Then Romanian

1. User opens app
2. Creates new post
3. Switches to English tab
4. Types English content
5. Taps "AI Actions" → "Translate to Swedish"
6. Reviews Swedish translation
7. Accepts and switches to Swedish tab
8. Proofreads Swedish
9. Taps "AI Actions" → "Translate to Romanian"
10. Reviews Romanian translation
11. Saves post

### Typical Workflow 3: Work on Multiple Posts Simultaneously

1. User has Post A open (LinkedIn announcement)
2. Starts work on Post B (blog summary) without closing Post A
3. Taps "Post Switcher" in bottom toolbar
4. Sees both Post A and Post B listed
5. Taps Post B to switch
6. Works on Post B
7. Switches back to Post A via Post Switcher
8. Closes Post A when finished
9. Post B remains open

### Typical Workflow 4: Restore Previous Version

1. User proofreads text and accepts changes
2. Then translates but doesn't like result
3. Taps "Version History"
4. Sees versions before and after translation
5. Taps version before translation
6. Views that version's content
7. Taps "Restore"
8. Post reverts to pre-translation state
9. User can try translation again with different approach

---

## Data and Storage

### What Gets Saved

Each post stores:
- **Content:** Text for all three languages
- **Metadata:** Creation date, last modified date, workflow stage
- **History:** Up to 50 previous versions with timestamps

### Where It's Saved

- **Local device:** All posts saved to app-specific storage
- **Cloud:** If sync enabled, posts synced to user's cloud account
- **Secure storage:** API key stored in secure keychain/keystore

### Data Format

- Posts saved as structured data (JSON format)
- Human-readable if opened in text editor
- Can be backed up with device backups

### Data Privacy

- All data stored locally or in user's personal cloud account
- API key stored securely, never shared
- No data sent to servers except Claude API for AI operations
- No analytics or tracking

---

## Error Handling

### API Errors

**Invalid API Key:**
- Message: "API key not configured or invalid"
- Action: Direct user to Settings to enter valid key

**Network Error:**
- Message: "Network error - check your connection"
- Action: Retry button available

**Rate Limit:**
- Message: "API rate limit exceeded - please wait and try again"
- Action: User waits and retries

**Timeout:**
- Message: "Request timed out - please try again"
- Action: Retry button available

**General API Error:**
- Message: Specific error from API
- Action: Retry or contact support

### Storage Errors

**Save Failed:**
- Message: "Failed to save post"
- Action: Retry save, check device storage

**Load Failed:**
- Message: "Failed to load post"
- Action: Retry, post may be corrupted

### Sync Errors

**No Internet:**
- Status: "Offline" indicator
- Message: "No internet connection"
- Action: Posts saved locally, will sync when online

**Cloud Not Available:**
- Status: "Offline" indicator
- Message: "Cloud service unavailable"
- Action: User checks cloud account status

**Sync Conflict:**
- Automatically resolved (latest wins)
- No user action needed

---

## Settings and Configuration

### Required Configuration

**Claude API Key:**
- User must provide their own API key from Anthropic
- Entered in Settings screen
- Stored securely
- Validated on first use
- App cannot function without valid key

### Optional Configuration

**Markdown Mode:**
- Default: OFF
- User preference for output format

**Cloud Sync:**
- Default: ON
- Can be disabled for local-only storage

---

## Platform Requirements

### iOS Version
- Minimum: iOS 16.0
- Supports latest iOS features

### Android Version (for implementation)
- Recommended: Android 8.0+ (API 26+)
- Material Design guidelines

### Network
- Required for AI operations
- Required for cloud sync
- App works offline for editing only

### Cloud Account
- iOS: iCloud account
- Android: Google account
- Required only if sync enabled

---

## Character Limits and Constraints

### LinkedIn Limits
- Maximum: 3000 characters per post
- App enforces this limit across all languages
- Visual indicators at 90% (2700 chars) and 100% (3000 chars)

### Version History
- Maximum: 50 versions per post
- Oldest automatically removed when exceeded

### Multiple Posts
- No limit on number of saved posts
- Limited only by device storage
- Cloud sync limited by user's cloud storage quota

---

## Use Cases and Scenarios

### Use Case 1: Bilingual Professional
"I write LinkedIn posts in Swedish for my local network, but want to share English versions with my international colleagues."

**Solution:** Write in Swedish, proofread, then translate to English. Review both versions and post separately or as multilingual content.

### Use Case 2: Content Repurposing
"I write blog posts in English and want to share key points on LinkedIn in multiple languages."

**Solution:** Write condensed version in English, translate to Swedish and Romanian, adjust as needed for cultural context.

### Use Case 3: Translation Quality Check
"I want to make sure my translations maintain the original meaning."

**Solution:** Write Swedish post, translate to English, then back-translate to Swedish to verify meaning preservation. Use translation help for specific phrases.

### Use Case 4: Multi-Device Workflow
"I start posts on my phone during commute, finish on tablet at home."

**Solution:** Enable cloud sync. Start post on phone, auto-syncs. Open app on tablet, post appears automatically. Continue editing seamlessly.

### Use Case 5: Iterative Refinement
"I like to try different versions of my posts before finalizing."

**Solution:** Write draft, save version. Try different AI operations. If don't like result, restore previous version. Repeat until satisfied.

---

## Future Considerations (Not Currently Implemented)

The following features are NOT in the current app but could be added:

- Direct posting to LinkedIn (requires LinkedIn API integration)
- More languages beyond Swedish/English/Romanian
- Team collaboration and sharing
- Post templates
- Scheduled posting
- Analytics on post performance
- AI-generated content from scratch (currently AI only edits existing content)
- Image attachment support
- Hashtag suggestions
- Export to other formats (PDF, Word, etc.)

---

## Technical Requirements for Implementation

### APIs Required
- Claude API (Anthropic) - for AI features
- Cloud sync API (iCloud/CloudKit for iOS, Firebase/Google Drive for Android)

### Permissions Required
- Internet access (for API calls)
- Storage access (for saving posts)
- Cloud account access (for sync, if enabled)
- Keychain/Keystore access (for secure API key storage)

### Performance Expectations
- AI operations: 3-30 seconds depending on text length
- Local save: < 1 second
- Cloud sync: 1-5 seconds depending on connection
- App launch: < 2 seconds

### Offline Capabilities
- Text editing: Fully functional
- Viewing saved posts: Fully functional
- AI operations: Not available (requires internet)
- Cloud sync: Queued until online

---

## Accessibility Considerations

- Text editors should support platform screen readers
- All buttons should have descriptive labels
- Color should not be only indicator (workflow stages should have text)
- Font sizes should be adjustable
- High contrast mode support

---

## Localization

**Current Implementation:**
- User interface in English
- Content editing in Swedish, English, Romanian

**For Android Implementation:**
- Consider localizing UI to Swedish, Romanian
- All user-facing messages should be translatable
- Date/time formats should be locale-appropriate

---

## End of Functional Specification

This document describes the complete functionality of LinkedIn Communicator as implemented in the iOS version. Use this as the source of truth for implementing the Android version.
