unit RecordInitializeFinalizeSelfTypes;

interface

type
  TFoo = record
    class operator Initialize(out Self: TFoo);
    class operator Finalize(var Self: TFoo);
  end;

  TBar = record
    class operator Initialize;
    class operator Finalize;
  end;

implementation

procedure Test(Foo: TFoo); overload;
begin
  // Do nothing
end;

procedure Test(Bar: TBar); overload;
begin
  // Do nothing
end;

class operator TFoo.Initialize(out Self: TFoo);
begin
  Test(Self);
end;

class operator TFoo.Finalize(var Self: TFoo);
begin
  Test(Self);
end;

class operator TBar.Initialize;
begin
  Test(Self);
end;

class operator TBar.Finalize;
begin
  Test(Self);
end;

end.
