unit Vcl.Dialogs;

interface

uses
  System.UITypes;

const
  mtWarning	= System.UITypes.TMsgDlgType.mtWarning;
  mtError	= System.UITypes.TMsgDlgType.mtError;
  mtInformation	= System.UITypes.TMsgDlgType.mtInformation;
  mtConfirmation	= System.UITypes.TMsgDlgType.mtConfirmation;
  mtCustom	= System.UITypes.TMsgDlgType.mtCustom;

  mbYes	= System.UITypes.TMsgDlgBtn.mbYes;
  mbNo	= System.UITypes.TMsgDlgBtn.mbNo;
  mbOK	= System.UITypes.TMsgDlgBtn.mbOK;
  mbCancel	= System.UITypes.TMsgDlgBtn.mbCancel;
  mbAbort	= System.UITypes.TMsgDlgBtn.mbAbort;
  mbRetry	= System.UITypes.TMsgDlgBtn.mbRetry;
  mbIgnore	= System.UITypes.TMsgDlgBtn.mbIgnore;
  mbAll	= System.UITypes.TMsgDlgBtn.mbAll;
  mbNoToAll	= System.UITypes.TMsgDlgBtn.mbNoToAll;
  mbYesToAll	= System.UITypes.TMsgDlgBtn.mbYesToAll;
  mbHelp	= System.UITypes.TMsgDlgBtn.mbHelp;
  mbClose	= System.UITypes.TMsgDlgBtn.mbClose;

function MessageDlg(const Msg: string; DlgType: TMsgDlgType;
  Buttons: TMsgDlgButtons; HelpCtx: Longint): Integer; overload; inline;
function MessageDlg(const Msg: string; DlgType: TMsgDlgType;
  Buttons: TMsgDlgButtons; HelpCtx: Longint; DefaultButton: TMsgDlgBtn): Integer; overload; inline;
function MessageDlgPosHelp(const Msg: string; DlgType: TMsgDlgType;
  Buttons: TMsgDlgButtons; HelpCtx: Longint; X, Y: Integer;
  const HelpFileName: string): Integer; overload;
function MessageDlgPosHelp(const Msg: string; DlgType: TMsgDlgType;
  Buttons: TMsgDlgButtons; HelpCtx: Longint; X, Y: Integer;
  const HelpFileName: string; DefaultButton: TMsgDlgBtn): Integer; overload;

implementation

function MessageDlg(const Msg: string; DlgType: TMsgDlgType;
  Buttons: TMsgDlgButtons; HelpCtx: Longint): Integer;
begin
  Result := MessageDlgPosHelp(Msg, DlgType, Buttons, HelpCtx, -1, -1, '');
end;

function MessageDlg(const Msg: string; DlgType: TMsgDlgType;
  Buttons: TMsgDlgButtons; HelpCtx: Longint; DefaultButton: TMsgDlgBtn): Integer; overload;
begin
  Result := MessageDlgPosHelp(Msg, DlgType, Buttons, HelpCtx, -1, -1, '', DefaultButton);
end;

function MessageDlgPosHelp(const Msg: string; DlgType: TMsgDlgType;
  Buttons: TMsgDlgButtons; HelpCtx: Longint; X, Y: Integer;
  const HelpFileName: string): Integer;
begin

end;

function MessageDlgPosHelp(const Msg: string; DlgType: TMsgDlgType;
  Buttons: TMsgDlgButtons; HelpCtx: Longint; X, Y: Integer;
  const HelpFileName: string; DefaultButton: TMsgDlgBtn): Integer; overload;
begin

end;

end.