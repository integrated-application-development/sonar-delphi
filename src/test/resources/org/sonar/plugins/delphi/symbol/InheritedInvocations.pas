unit InheritedInvocations;

{This is a sample Delphi file.}

interface

type
  TFoo = class(TObject)
  public
    procedure Proc(A: Integer; B: String);
    procedure Proc;
  end;

  TBar = class(TFoo)
  public
    procedure Proc(A: Integer; B: String); override; overload;
    procedure Proc; override; overload;
    procedure Proc(C: String); overload;
  end;

implementation

  procedure TFoo.Proc(A: Integer; B: String);
  begin
    // Do nothing
  end;

  procedure TFoo.Proc;
  begin
    // Do nothing
  end;

  procedure TBar.Proc(A: Integer; B: String);
  begin
    inherited;
    inherited Proc;
    inherited Proc(A, B);
    Proc;
  end;

  procedure TBar.Proc;
  begin
    inherited;
    inherited Proc;
    inherited Proc(1, 'test');
    Proc(1, 'test');
    Proc('test');
  end;

  procedure TBar.Proc(C: String);
  begin
    // Do nothing
  end;

end.