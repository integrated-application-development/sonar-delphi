unit ClassHelperAccessingExtendedType;

{This is a sample Delphi file.}

interface

type
  TBaseFoo = class
  private
    procedure BasePrivateBar;
  protected
    procedure BaseProtectedBar;
  end;

  TFoo = class(TBaseFoo)
  private
    procedure PrivateBar;
  protected
    procedure ProtectedBar;
  end;

  TFooHelper = class helper for TFoo
    procedure Test;
  end;

implementation

procedure TBaseFoo.BasePrivateBar;
begin
  // Do nothing
end;

procedure TBaseFoo.BaseProtectedBar;
begin
  // Do nothing
end;


procedure TFoo.PrivateBar;
begin
  // Do nothing
end;

procedure TFoo.ProtectedBar;
begin
  // Do nothing
end;

procedure TFooHelper.Test;
begin
  BasePrivateBar;
  PrivateBar;

  BaseProtectedBar;
  ProtectedBar;
end;
end.