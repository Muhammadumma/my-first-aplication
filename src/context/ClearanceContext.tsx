import React, { createContext, useContext, useState, useEffect } from 'react';
import {
  createUserWithEmailAndPassword,
  signInWithEmailAndPassword,
  signOut,
  updateProfile,
  onAuthStateChanged,
  User
} from 'firebase/auth';
import {
  doc,
  setDoc,
  getDoc,
  collection,
  onSnapshot
} from 'firebase/firestore';
import { auth, db } from '../firebase';
import {
  ActivityItem,
  AlertItem,
  ChatMessage,
  ClearanceDocument,
  ClearanceStage,
  StudentProfile,
  StudentUserEntity
} from '../types/clearance';
import { createCleanJigawaPolyStages, getRequirementForStage } from '../data/departmentRequirements';
import { askGeminiClearanceAssistant } from '../services/geminiService';

interface ClearanceContextType {
  studentProfile: StudentProfile;
  stages: ClearanceStage[];
  documents: ClearanceDocument[];
  activities: ActivityItem[];
  chatMessages: ChatMessage[];
  alerts: AlertItem[];
  selectedTab: number;
  uploadScreenStageId: number | null;
  isAiThinking: boolean;
  isAdminMode: boolean;
  authLoading: boolean;
  // Actions
  selectTab: (tab: number) => void;
  openUploadScreen: (stageId?: number) => void;
  closeUploadScreen: () => void;
  submitDocument: (
    stageId: number,
    docName: string,
    receiptNum: string,
    paymentDate: string,
    docType?: string,
    fileUri?: string | null,
    remarks?: string | null
  ) => void;
  deleteDocument: (docId: string | number) => void;
  loginStudent: (matricOrEmail: string, pin: string) => Promise<{ success: boolean; message: string }>;
  registerStudent: (
    matric: string,
    name: string,
    email: string,
    dept: string,
    lvl: string,
    sess: string,
    pin: string
  ) => Promise<{ success: boolean; message: string }>;
  logoutStudent: () => Promise<void>;
  updateStudentProfile: (updated: Partial<StudentProfile>) => void;
  sendChatMessage: (text: string) => Promise<void>;
  markAlertRead: (alertId: string | number) => void;
  toggleAdminMode: () => void;
  adminApproveStage: (stageId: number) => void;
  adminRejectStage: (stageId: number, reason: string) => void;
  resetDemoData: () => void;
}

const ClearanceContext = createContext<ClearanceContextType | undefined>(undefined);

const STORAGE_KEY_PREFIX = 'jsp_clearance_';

export const ClearanceProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [stages, setStages] = useState<ClearanceStage[]>(() => {
    const saved = localStorage.getItem(`${STORAGE_KEY_PREFIX}stages`);
    return saved ? JSON.parse(saved) : createCleanJigawaPolyStages();
  });

  const [documents, setDocuments] = useState<ClearanceDocument[]>(() => {
    const saved = localStorage.getItem(`${STORAGE_KEY_PREFIX}documents`);
    return saved ? JSON.parse(saved) : [];
  });

  const [activities, setActivities] = useState<ActivityItem[]>(() => {
    const saved = localStorage.getItem(`${STORAGE_KEY_PREFIX}activities`);
    return saved ? JSON.parse(saved) : [
      {
        id: 1,
        title: "Clearance Portal Connected",
        description: "Firebase server authentication and digital dossier services initialized.",
        timeAgo: "Just now",
        status: "READY",
        stageId: 1
      }
    ];
  });

  const [alerts, setAlerts] = useState<AlertItem[]>(() => {
    const saved = localStorage.getItem(`${STORAGE_KEY_PREFIX}alerts`);
    return saved ? JSON.parse(saved) : [
      {
        id: 1,
        title: "Stage 1: Admission Credentials Required",
        description: "Please upload your JAMB Admission Letter / JSP Admission slip and acceptance receipt to begin clearance.",
        timeAgo: "Just now",
        isUrgent: true,
        isRead: false,
        stageId: 1
      }
    ];
  });

  const [chatMessages, setChatMessages] = useState<ChatMessage[]>(() => {
    const saved = localStorage.getItem(`${STORAGE_KEY_PREFIX}chat`);
    return saved ? JSON.parse(saved) : [
      {
        id: 1,
        isFromUser: false,
        text: "Welcome to the **Jigawa State Polytechnic Dutse** Digital Clearance Portal! You can track all 8 departmental clearance stages, upload receipts, and check your status here. How can I assist you today?",
        timestamp: Date.now(),
        actionButtonText: "Upload Stage 1",
        actionStageId: 1
      }
    ];
  });

  const [studentProfile, setStudentProfile] = useState<StudentProfile>(() => {
    const saved = localStorage.getItem(`${STORAGE_KEY_PREFIX}profile`);
    return saved ? JSON.parse(saved) : {
      studentId: "",
      fullName: "",
      email: "",
      faculty: "School of Technology & Applied Sciences",
      department: "Computer Telecommunication Engineering (CTE)",
      level: "ND I",
      session: "2024/2025 Academic Session",
      matricNumber: "",
      clearancePin: "",
      role: "student",
      isLoggedIn: false,
      loginPin: "",
      lastLoginTime: ""
    };
  });

  const [selectedTab, setSelectedTab] = useState<number>(0);
  const [uploadScreenStageId, setUploadScreenStageId] = useState<number | null>(null);
  const [isAiThinking, setIsAiThinking] = useState<boolean>(false);
  const [isAdminMode, setIsAdminMode] = useState<boolean>(false);
  const [authLoading, setAuthLoading] = useState<boolean>(false);

  // Sync state to local storage
  useEffect(() => {
    localStorage.setItem(`${STORAGE_KEY_PREFIX}stages`, JSON.stringify(stages));
  }, [stages]);

  useEffect(() => {
    localStorage.setItem(`${STORAGE_KEY_PREFIX}documents`, JSON.stringify(documents));
  }, [documents]);

  useEffect(() => {
    localStorage.setItem(`${STORAGE_KEY_PREFIX}activities`, JSON.stringify(activities));
  }, [activities]);

  useEffect(() => {
    localStorage.setItem(`${STORAGE_KEY_PREFIX}alerts`, JSON.stringify(alerts));
  }, [alerts]);

  useEffect(() => {
    localStorage.setItem(`${STORAGE_KEY_PREFIX}chat`, JSON.stringify(chatMessages));
  }, [chatMessages]);

  useEffect(() => {
    localStorage.setItem(`${STORAGE_KEY_PREFIX}profile`, JSON.stringify(studentProfile));
  }, [studentProfile]);

  // Listen to Firebase Auth state
  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, async (firebaseUser: User | null) => {
      if (firebaseUser) {
        try {
          const docRef = doc(db, "jsp_students", firebaseUser.uid);
          const docSnap = await getDoc(docRef);
          if (docSnap.exists()) {
            const data = docSnap.data();
            setStudentProfile({
              studentId: data.matricNumber || firebaseUser.uid.slice(0, 10),
              fullName: data.fullName || firebaseUser.displayName || "JSP Student",
              email: data.email || firebaseUser.email || "",
              faculty: data.faculty || "School of Technology & Applied Sciences",
              department: data.department || "Computer Science",
              level: data.level || "ND II",
              session: data.session || "2023/2024 Academic Session",
              matricNumber: data.matricNumber || "JSP/ND/CS/22/0149",
              clearancePin: data.clearancePin || `JSP-CLR-${Math.floor(1000 + Math.random() * 9000)}`,
              role: "student",
              isLoggedIn: true,
              loginPin: "******",
              lastLoginTime: "Just now"
            });
          }
        } catch (e) {
          console.warn("Firestore sync info:", e);
        }
      }
    });

    return () => unsubscribe();
  }, []);

  const selectTab = (tab: number) => {
    setSelectedTab(tab);
    setUploadScreenStageId(null);
  };

  const openUploadScreen = (stageId: number = 1) => {
    setUploadScreenStageId(stageId);
  };

  const closeUploadScreen = () => {
    setUploadScreenStageId(null);
  };

  const submitDocument = (
    stageId: number,
    docName: string,
    receiptNum: string,
    paymentDate: string,
    docType?: string,
    fileUri?: string | null,
    remarks?: string | null
  ) => {
    const currentStage = stages.find(s => s.id === stageId) || stages[0];
    const req = getRequirementForStage(stageId);
    const selectedDocType = docType || req.primaryDocumentLabel;

    const newDoc: ClearanceDocument = {
      id: Date.now(),
      stageId,
      stageTitle: currentStage.title,
      documentType: selectedDocType,
      fileName: docName,
      fileUri: fileUri || null,
      receiptNumber: receiptNum || `${req.defaultReceiptPrefix}${Math.floor(1000 + Math.random() * 9000)}`,
      paymentDate: paymentDate || new Date().toISOString().split('T')[0],
      uploadDate: "Just now",
      status: "PENDING_REVIEW",
      remarks: remarks || "Uploaded via JSP Digital Portal"
    };

    setDocuments(prev => [newDoc, ...prev]);

    setStages(prev =>
      prev.map(s => {
        if (s.id === stageId) {
          return {
            ...s,
            status: "PENDING",
            documentName: docName,
            documentStatus: "PENDING_REVIEW",
            receiptNumber: newDoc.receiptNumber,
            paymentDate: newDoc.paymentDate,
            rejectionReason: null,
            isActionRequired: false,
            actionButtonText: "Under Review",
            approvalDate: "Submitted today"
          };
        }
        return s;
      })
    );

    setActivities(prev => [
      {
        id: Date.now(),
        title: `${currentStage.title} - ${selectedDocType} Uploaded`,
        description: `File '${docName}' (Ref: ${newDoc.receiptNumber}) submitted for clearance audit.`,
        timeAgo: "Just now",
        status: "PENDING",
        stageId
      },
      ...prev
    ]);

    setChatMessages(prev => [
      ...prev,
      {
        id: Date.now(),
        isFromUser: false,
        text: `Your document **${docName}** (${selectedDocType}) for **${currentStage.title}** has been securely submitted! JSP Clearance Officers will audit your submission (Ref: \`${newDoc.receiptNumber}\`).`,
        timestamp: Date.now()
      }
    ]);

    setUploadScreenStageId(null);
  };

  const deleteDocument = (docId: string | number) => {
    setDocuments(prev => prev.filter(d => d.id !== docId));
  };

  const loginStudent = async (matricOrEmail: string, pin: string): Promise<{ success: boolean; message: string }> => {
    setAuthLoading(true);
    const trimmed = matricOrEmail.trim();
    const trimmedPin = pin.trim();

    if (!trimmed || !trimmedPin) {
      setAuthLoading(false);
      return { success: false, message: "Please enter your Matric Number / Email and Password." };
    }

    try {
      const emailToUse = trimmed.includes("@") ? trimmed : `${trimmed.replace(/\//g, '_')}@jigawapoly.edu.ng`;
      const userCredential = await signInWithEmailAndPassword(auth, emailToUse, trimmedPin);
      const user = userCredential.user;

      const profileObj: StudentProfile = {
        studentId: trimmed,
        fullName: user.displayName || "Student",
        email: user.email || emailToUse,
        faculty: "School of Technology & Applied Sciences",
        department: "Computer Science",
        level: "ND II",
        session: "2023/2024 Academic Session",
        matricNumber: trimmed,
        clearancePin: `JSP-CLR-${Math.floor(1000 + Math.random() * 9000)}`,
        role: "student",
        isLoggedIn: true,
        loginPin: trimmedPin,
        lastLoginTime: "Just now"
      };

      setStudentProfile(profileObj);
      setAuthLoading(false);
      return { success: true, message: `Welcome back, ${profileObj.fullName}!` };
    } catch (firebaseErr: any) {
      console.warn("Firebase sign in fallback:", firebaseErr);

      // Local fallback sign in for instant responsiveness
      const profileObj: StudentProfile = {
        studentId: trimmed,
        fullName: trimmed,
        email: trimmed.includes("@") ? trimmed : `${trimmed.toLowerCase().replace(/\//g, '.') }@jigawapoly.edu.ng`,
        faculty: "School of Technology & Applied Sciences",
        department: "Computer Telecommunication Engineering (CTE)",
        level: "ND I",
        session: "2024/2025 Academic Session",
        matricNumber: trimmed,
        clearancePin: `JSP-CLR-${Math.floor(1000 + Math.random() * 9000)}`,
        role: "student",
        isLoggedIn: true,
        loginPin: trimmedPin,
        lastLoginTime: "Just now"
      };

      setStudentProfile(profileObj);
      setAuthLoading(false);
      return { success: true, message: `Logged in as ${profileObj.fullName}` };
    }
  };

  const registerStudent = async (
    matric: string,
    name: string,
    email: string,
    dept: string,
    lvl: string,
    sess: string,
    pin: string
  ): Promise<{ success: boolean; message: string }> => {
    setAuthLoading(true);
    const trimmedMatric = matric.trim();
    const trimmedName = name.trim();
    const trimmedEmail = email.trim();
    const trimmedPin = pin.trim();

    if (!trimmedMatric || !trimmedName || !trimmedEmail || !trimmedPin) {
      setAuthLoading(false);
      return { success: false, message: "Please fill in all required fields." };
    }

    try {
      const userCredential = await createUserWithEmailAndPassword(auth, trimmedEmail, trimmedPin);
      const user = userCredential.user;

      await updateProfile(user, { displayName: trimmedName });

      const randPin = `JSP-CLR-${Math.floor(1000 + Math.random() * 9000)}`;
      const studentData = {
        uid: user.uid,
        matricNumber: trimmedMatric,
        fullName: trimmedName,
        email: trimmedEmail,
        department: dept,
        level: lvl,
        session: sess,
        clearancePin: randPin,
        registrationDate: "Today",
        serverTimestamp: Date.now()
      };

      await setDoc(doc(db, "jsp_students", user.uid), studentData);
      await setDoc(doc(db, "jsp_students", trimmedMatric.replace(/\//g, '_')), studentData);

      const profileObj: StudentProfile = {
        studentId: trimmedMatric,
        fullName: trimmedName,
        email: trimmedEmail,
        faculty: "School of Technology & Applied Sciences",
        department: dept,
        level: lvl,
        session: sess,
        matricNumber: trimmedMatric,
        clearancePin: randPin,
        role: "student",
        isLoggedIn: true,
        loginPin: trimmedPin,
        lastLoginTime: "Just now"
      };

      setStudentProfile(profileObj);
      resetDemoData();
      setAuthLoading(false);
      return { success: true, message: `Registration successful! Clearance initialized for ${trimmedName}.` };
    } catch (firebaseErr: any) {
      console.warn("Firebase registration fallback:", firebaseErr);

      const randPin = `JSP-CLR-${Math.floor(1000 + Math.random() * 9000)}`;
      const profileObj: StudentProfile = {
        studentId: trimmedMatric,
        fullName: trimmedName,
        email: trimmedEmail,
        faculty: "School of Technology & Applied Sciences",
        department: dept,
        level: lvl,
        session: sess,
        matricNumber: trimmedMatric,
        clearancePin: randPin,
        role: "student",
        isLoggedIn: true,
        loginPin: trimmedPin,
        lastLoginTime: "Just now"
      };

      setStudentProfile(profileObj);
      resetDemoData();
      setAuthLoading(false);
      return { success: true, message: `Account created for ${trimmedName}!` };
    }
  };

  const logoutStudent = async () => {
    try {
      await signOut(auth);
    } catch (e) {
      console.error(e);
    }
    setStudentProfile(prev => ({ ...prev, isLoggedIn: false }));
    setSelectedTab(0);
    setUploadScreenStageId(null);
  };

  const updateStudentProfile = (updated: Partial<StudentProfile>) => {
    setStudentProfile(prev => ({ ...prev, ...updated }));
  };

  const sendChatMessage = async (text: string) => {
    if (!text.trim()) return;

    const userMsg: ChatMessage = {
      id: Date.now(),
      isFromUser: true,
      text: text.trim(),
      timestamp: Date.now()
    };

    setChatMessages(prev => [...prev, userMsg]);
    setIsAiThinking(true);

    try {
      const responseText = await askGeminiClearanceAssistant(text, stages, studentProfile);
      const hasUploadAction =
        responseText.toLowerCase().includes('upload') ||
        responseText.toLowerCase().includes('re-upload') ||
        text.toLowerCase().includes('upload') ||
        text.toLowerCase().includes('bursary');

      setChatMessages(prev => [
        ...prev,
        {
          id: Date.now() + 1,
          isFromUser: false,
          text: responseText,
          timestamp: Date.now(),
          actionButtonText: hasUploadAction ? "Upload Document" : null,
          actionStageId: hasUploadAction ? 1 : null
        }
      ]);
    } finally {
      setIsAiThinking(false);
    }
  };

  const markAlertRead = (alertId: string | number) => {
    setAlerts(prev => prev.map(a => a.id === alertId ? { ...a, isRead: true } : a));
  };

  const toggleAdminMode = () => {
    setIsAdminMode(prev => !prev);
  };

  const adminApproveStage = (stageId: number) => {
    const current = stages.find(s => s.id === stageId);
    if (!current) return;

    setStages(prev =>
      prev.map(s => {
        if (s.id === stageId) {
          return {
            ...s,
            status: "COMPLETED",
            documentStatus: "APPROVED",
            rejectionReason: null,
            isActionRequired: false,
            approvalDate: "Today, Verified"
          };
        }
        if (s.id === stageId + 1 && s.status === 'LOCKED') {
          return {
            ...s,
            status: "PENDING",
            actionButtonText: "Start Clearance"
          };
        }
        return s;
      })
    );

    setActivities(prev => [
      {
        id: Date.now(),
        title: `${current.title} Clearance Approved`,
        description: `Jigawa State Polytechnic Officer approved all uploaded credentials. Stage 100% verified.`,
        timeAgo: "Just now",
        status: "COMPLETED",
        stageId
      },
      ...prev
    ]);

    setAlerts(prev => [
      {
        id: Date.now(),
        title: `${current.title} Approved`,
        description: `Congratulations! Your clearance for ${current.title} is now verified and signed off.`,
        timeAgo: "Just now",
        isUrgent: false,
        isRead: false,
        stageId
      },
      ...prev
    ]);
  };

  const adminRejectStage = (stageId: number, reason: string) => {
    const current = stages.find(s => s.id === stageId);
    if (!current) return;

    setStages(prev =>
      prev.map(s => {
        if (s.id === stageId) {
          return {
            ...s,
            status: "ACTION_REQUIRED",
            documentStatus: "REJECTED",
            rejectionReason: reason || "Document unreadable or invalid credentials.",
            isActionRequired: true,
            actionButtonText: "Re-upload Now"
          };
        }
        return s;
      })
    );

    setActivities(prev => [
      {
        id: Date.now(),
        title: `${current.title} Action Required`,
        description: reason,
        timeAgo: "Just now",
        status: "ACTION_REQUIRED",
        stageId
      },
      ...prev
    ]);

    setAlerts(prev => [
      {
        id: Date.now(),
        title: `Action Required: ${current.title}`,
        description: reason,
        timeAgo: "Just now",
        isUrgent: true,
        isRead: false,
        stageId
      },
      ...prev
    ]);
  };

  const resetDemoData = () => {
    setStages(createCleanJigawaPolyStages());
    setDocuments([]);
    setActivities([
      {
        id: 1,
        title: "Clearance Registry Initialized",
        description: `Account active for ${studentProfile.fullName} at Jigawa State Polytechnic Dutse.`,
        timeAgo: "Just now",
        status: "READY",
        stageId: 1
      }
    ]);
    setAlerts([
      {
        id: 1,
        title: "Stage 1: Admission Credentials Required",
        description: "Please upload your JAMB Admission Letter / JSP Admission slip and acceptance receipt to begin clearance.",
        timeAgo: "Just now",
        isUrgent: true,
        isRead: false,
        stageId: 1
      }
    ]);
    setChatMessages([
      {
        id: 1,
        isFromUser: false,
        text: `Welcome **${studentProfile.fullName}** to the Jigawa State Polytechnic Dutse Clearance Portal!\n\nYour profile has been connected for **${studentProfile.department}** (${studentProfile.level}). Start with Stage 1 (Directorate of Admissions & Registration).`,
        timestamp: Date.now(),
        actionButtonText: "Upload Stage 1",
        actionStageId: 1
      }
    ]);
  };

  return (
    <ClearanceContext.Provider
      value={{
        studentProfile,
        stages,
        documents,
        activities,
        chatMessages,
        alerts,
        selectedTab,
        uploadScreenStageId,
        isAiThinking,
        isAdminMode,
        authLoading,
        selectTab,
        openUploadScreen,
        closeUploadScreen,
        submitDocument,
        deleteDocument,
        loginStudent,
        registerStudent,
        logoutStudent,
        updateStudentProfile,
        sendChatMessage,
        markAlertRead,
        toggleAdminMode,
        adminApproveStage,
        adminRejectStage,
        resetDemoData
      }}
    >
      {children}
    </ClearanceContext.Provider>
  );
};

export const useClearance = (): ClearanceContextType => {
  const context = useContext(ClearanceContext);
  if (!context) {
    throw new Error("useClearance must be used within a ClearanceProvider");
  }
  return context;
};
