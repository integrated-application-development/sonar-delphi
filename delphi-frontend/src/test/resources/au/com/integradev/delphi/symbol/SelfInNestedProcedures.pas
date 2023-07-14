unit SelfInNestedProcedures;

interface

type
  TBar = class(TObject)
  end;

  TBaz = class(TObject)
  end;

  TFoo = class(TObject)
    var Self: TBar;
    procedure MyProc;
  end;

 procedure Test(Obj: TFoo); overload;
 procedure Test(Obj: TBar); overload;
 procedure Test(Obj: TBaz); overload;

implementation

uses System.SysUtils;

procedure TFoo.MyProc;

  procedure SubProc(Self: TBaz);
  begin
    Test(Self); // Test(TBaz)
  end;

var Anonymous: TProc<TBaz>;
begin
  Test(Self); // Test(TFoo)
  SubProc(TBaz.Create);

  Anonymous := procedure(Self: TBaz) begin
    Test(Self); // Test(TBaz)
  end;

  Anonymous(TBaz.Create);
end;

procedure Test(Obj: TFoo);
begin
end;

procedure Test(Obj: TBar);
begin
end;

procedure Test(Obj: TBaz);
begin
end;

end.
